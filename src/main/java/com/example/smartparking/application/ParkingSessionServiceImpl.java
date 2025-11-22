package com.example.smartparking.application;

import com.example.smartparking.application.mapper.AvailabilityEventMapper;
import com.example.smartparking.application.mapper.ParkingSessionMapper;
import com.example.smartparking.domain.*;
import com.example.smartparking.domain.strategy.AvailabilityEventPublisher;
import com.example.smartparking.domain.strategy.FeeCalculationStrategy;
import com.example.smartparking.domain.strategy.SpotAllocationStrategy;
import com.example.smartparking.repository.*;
import com.example.smartparking.observability.ParkingMetrics;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ParkingSessionServiceImpl implements ParkingSessionService {

    private static final Logger log =
            LoggerFactory.getLogger(ParkingSessionServiceImpl.class);

    private static final String DEFAULT_CURRENCY = "INR";

    private final LotRepository lotRepository;
    private final VehicleRepository vehicleRepository;
    private final SpotRepository spotRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final SpotAllocationStrategy allocationStrategy;
    private final FeeCalculationStrategy feeStrategy;
    private final AvailabilityEventPublisher availabilityPublisher;
    private final ParkingSessionMapper sessionMapper;
    private final AvailabilityEventMapper availabilityEventMapper;
    private final ParkingMetrics parkingMetrics;

    public ParkingSessionServiceImpl(
            LotRepository lotRepository,
            VehicleRepository vehicleRepository,
            SpotRepository spotRepository,
            TicketRepository ticketRepository,
            PaymentRepository paymentRepository,
            SpotAllocationStrategy allocationStrategy,
            FeeCalculationStrategy feeStrategy,
            AvailabilityEventPublisher availabilityPublisher,
            ParkingSessionMapper sessionMapper,
            AvailabilityEventMapper availabilityEventMapper,
            ParkingMetrics parkingMetrics
    ) {
        this.lotRepository = lotRepository;
        this.vehicleRepository = vehicleRepository;
        this.spotRepository = spotRepository;
        this.ticketRepository = ticketRepository;
        this.paymentRepository = paymentRepository;
        this.allocationStrategy = allocationStrategy;
        this.feeStrategy = feeStrategy;
        this.availabilityPublisher = availabilityPublisher;
        this.sessionMapper = sessionMapper;
        this.availabilityEventMapper = availabilityEventMapper;
        this.parkingMetrics = parkingMetrics;
    }

    // ---------- Check-in ----------

    @Override
    @Transactional
    public CheckInResult checkIn(CheckInCommand command) {
        log.info("Check-in started lotId={} entranceId={} plate={} size={} reservationId={}",
                command.lotId(), command.entranceId(), command.licensePlate(),
                command.vehicleSize(), command.reservationId().orElse(null));
        // Ensure lot exists
        Lot lot = lotRepository.findById(command.lotId())
                .orElseThrow(() -> new NotFoundException("Lot not found"));
        // Find or create vehicle
        Vehicle vehicle = vehicleRepository.findByLicensePlate(command.licensePlate())
                .orElseGet(() -> {
                    Vehicle v = new Vehicle(
                            UUID.randomUUID(),
                            command.licensePlate(),
                            command.vehicleSize()
                    );
                    log.debug("Registering new vehicle id={} plate={} size={}",
                            v.getId(), v.getLicensePlate(), v.getSize());
                    return vehicleRepository.save(v);
                });


        // Enforce: one OPEN ticket per vehicle per lot (constraint + explicit check)
        Optional<Ticket> existingOpenTicket =
                ticketRepository.findByVehicleIdAndLotIdAndStatus(
                        vehicle.getId(),
                        command.lotId(),
                        TicketStatus.OPEN
                );

        if (existingOpenTicket.isPresent()) {
            log.warn("Check-in rejected: existing OPEN ticketId={} for lotId={} plate={}",
                    existingOpenTicket.get().getId(), command.lotId(), command.licensePlate());
            parkingMetrics.onCheckInConflict();
            throw new ConflictException("Vehicle already has an active ticket in this lot");
        }

        // Delegate to allocation strategy
        SpotAllocationStrategy.AllocationRequest allocationRequest =
                new SpotAllocationStrategy.AllocationRequest(
                        command.lotId(),
                        command.entranceId(),
                        command.vehicleSize(),
                        command.reservationId(),
                        command.requestedAt()
                );

        SpotAllocationStrategy.AllocationResult allocationResult =
                allocationStrategy.allocateSpot(allocationRequest)
                        .orElseThrow(() -> {
                            log.info("No suitable spot available lotId={} plate={} size={}",
                                    command.lotId(), command.licensePlate(), command.vehicleSize());
                            return new BusinessRuleException(
                                    "No suitable spot available",
                                    "no_spot_available"
                            );
                        });

        Spot spot = spotRepository.findById(allocationResult.spotId())
                .orElseThrow(() -> new NotFoundException("Allocated spot not found"));

        // Change spot status to OCCUPIED and persist
        spot.occupy();
        spotRepository.save(spot);

        // Create OPEN ticket
        Instant entryAt = command.requestedAt();
        Ticket ticket = new Ticket(
                UUID.randomUUID(),
                spot.getId(),
                vehicle.getId(),
                lot.getId(),
                entryAt
        );
        ticketRepository.save(ticket);

        parkingMetrics.onCheckInSuccess();
        // Publish availability event
        availabilityPublisher.publish(
                availabilityEventMapper.toEvent(
                        AvailabilityEventPublisher.EventType.SPOT_OCCUPIED,
                        lot.getId(),
                        spot.getFloorId(),
                        spot,
                        entryAt
                )
        );

        log.info("Check-in success ticketId={} lotId={} spotId={} spotCode={} plate={}",
                ticket.getId(), lot.getId(), spot.getId(), spot.getCode(), command.licensePlate());

        // Map to DTO
        return sessionMapper.toCheckInResult(ticket, spot);
    }

    // ---------- Check-out ----------

    @Override
    @Transactional
    public CheckOutResult checkOut(CheckOutCommand command) {
        log.info("Check-out started lotId={} ticketId={}",
                command.lotId(), command.ticketId());

        // Load ticket
        Ticket ticket = ticketRepository.findById(command.ticketId())
                .orElseThrow(() -> {
                    log.warn("Check-out ticket not found ticketId={} lotId={}",
                            command.ticketId(), command.lotId());
                    return new NotFoundException("Ticket not found");
                });

        if (!ticket.isOpen()) {
            log.warn("Check-out rejected: ticket already closed ticketId={} status={}",
                    ticket.getId(), ticket.getStatus());
            throw new BusinessRuleException(
                    "Ticket already closed",
                    "ticket_closed"
            );
        }

        // Close ticket (domain enforces exit >= entry)
        ticket.close(command.exitAt());
        ticketRepository.save(ticket);

        // Free the spot
        Spot spot = spotRepository.findById(ticket.getSpotId())
                .orElseThrow(() -> new NotFoundException("Spot not found for ticket"));

        spot.markAvailable();
        spotRepository.save(spot);

        // Calculate fee (using configured strategy)
        String currency = DEFAULT_CURRENCY; // can be derived from lot/rate card later

        FeeCalculationStrategy.FeeBreakdown breakdown =
                feeStrategy.calculate(new FeeCalculationStrategy.FeeRequest(
                        ticket.getLotId(),
                        ticket.getId(),
                        ticket.getEntryAt(),
                        command.exitAt(),
                        currency
                ));

        // Create payment and mark successful
        Payment payment = new Payment(
                UUID.randomUUID(),
                ticket.getId(),
                breakdown.totalAmountMinor(),
                currency,
                PaymentMethod.CASH
        );
        payment.succeed("PAY-" + ticket.getId(), command.exitAt());
        paymentRepository.save(payment);

        // Publish availability event
        availabilityPublisher.publish(
                availabilityEventMapper.toEvent(
                        AvailabilityEventPublisher.EventType.SPOT_RELEASED,
                        ticket.getLotId(),
                        spot.getFloorId(),
                        spot,
                        command.exitAt()
                )
        );

        parkingMetrics.onCheckOutSuccess();

        log.info("Check-out success ticketId={} lotId={} spotId={} amountMinor={} currency={}",
                ticket.getId(), ticket.getLotId(), spot.getId(),
                breakdown.totalAmountMinor(), "INR");

        // Map to DTO
        return sessionMapper.toCheckOutResult(ticket, payment);
    }
}