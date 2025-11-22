package com.example.smartparking.application;

import com.example.smartparking.application.mapper.ParkingQueryMapper;
import com.example.smartparking.domain.SpotSize;
import com.example.smartparking.domain.SpotStatus;
import com.example.smartparking.domain.TicketStatus;
import com.example.smartparking.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional(readOnly = true)
public class ParkingQueryServiceImpl implements ParkingQueryService {
    private static final Logger log =
            LoggerFactory.getLogger(ParkingQueryServiceImpl.class);
    private final LotRepository lotRepository;
    private final FloorRepository floorRepository;
    private final SpotRepository spotRepository;
    private final VehicleRepository vehicleRepository;
    private final TicketRepository ticketRepository;
    private final ParkingQueryMapper mapper;

    public ParkingQueryServiceImpl(LotRepository lotRepository,
                                   FloorRepository floorRepository,
                                   SpotRepository spotRepository,
                                   VehicleRepository vehicleRepository,
                                   TicketRepository ticketRepository,
                                   ParkingQueryMapper parkingQueryMapper) {
        this.lotRepository = lotRepository;
        this.floorRepository = floorRepository;
        this.spotRepository = spotRepository;
        this.vehicleRepository = vehicleRepository;
        this.ticketRepository = ticketRepository;
        this.mapper = parkingQueryMapper;
    }

    public List<LotSummary> listLots() {
        return lotRepository.findAll()
                .stream()
                .map(mapper::toLotSummary)
                .collect(Collectors.toList());
    }

    public List<FloorSummary> listFloors(UUID lotId) {
        return floorRepository.findByLotIdOrderByOrderingAsc(lotId)
                .stream()
                .map(mapper::toFloorSummary)
                .collect(Collectors.toList());
    }

    public List<SpotSummary> listSpotsByFloor(UUID floorId) {
        return spotRepository.findByFloorId(floorId)
                .stream()
                .map(mapper::toSpotSummary)
                .collect(Collectors.toList());
    }

    public List<SpotSummary> listAvailableSpotsByFloor(UUID floorId, SpotSize size) {
        return spotRepository.findByFloorIdAndSizeAndStatus(
                        floorId,
                        size,
                        SpotStatus.AVAILABLE)
                .stream()
                .map(mapper::toSpotSummary)
                .collect(Collectors.toList());
    }

    public Optional<ActiveTicketSummary> findActiveTicketByVehicle(UUID lotId, String licensePlate) {
        log.debug("Finding active ticket lotId={} plate={}", lotId, licensePlate);

        return vehicleRepository.findByLicensePlate(licensePlate)
                .flatMap(vehicle ->
                        ticketRepository.findByVehicleIdAndLotIdAndStatus(
                                vehicle.getId(), lotId, TicketStatus.OPEN
                        )
                )
                .flatMap(ticket ->
                        spotRepository.findById(ticket.getSpotId())
                                .map(spot -> mapper.toActiveTicketSummary(ticket, spot))
                );
    }
}
