package com.example.smartparking.application;

import com.example.smartparking.domain.SpotSize;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ParkingSessionService {
    // checkIn service
    /**
     * Checks a vehicle into the lot:
     *  - finds or registers the vehicle,
     *  - allocates a compatible spot using the configured strategy,
     *  - creates an OPEN ticket,
     *  - publishes availability events.
     *
     * Throws:
     *  - NotFoundException if lot/entrance not found,
     *  - BusinessRuleException if no suitable spot is available,
     *  - ConflictException if the vehicle already has an OPEN ticket in this lot.
     */
    record CheckInCommand(
            UUID lotId,
            UUID entranceId,
            String licensePlate,
            SpotSize vehicleSize,
            Optional<UUID> reservationId,
            Instant requestedAt
    ){}
    record CheckInResult(
            UUID ticketId,
            UUID spotId,
            UUID floorId,
            String spotCode,
            SpotSize spotSize,
            Instant entryAt
    ){}
    CheckInResult checkIn(CheckInCommand command);

    //checkOut service
    /**
     * Checks a vehicle out of the lot:
     *  - loads and closes the OPEN ticket,
     *  - calculates the fee using the configured pricing strategy,
     *  - creates a Payment,
     *  - marks the spot AVAILABLE again and publishes events.
     *
     * Throws:
     *  - NotFoundException if the ticket does not exist,
     *  - BusinessRuleException if the ticket is already CLOSED or exitAt is invalid.
     */
    record CheckOutCommand(
            UUID lotId,
            UUID ticketId,
            Instant exitAt
    ){}
    record CheckOutResult(
            UUID ticketId,
            UUID paymentId,
            long amountMinor,
            String currency,
            Instant entryAt,
            Instant exitAt
    ) {}
    CheckOutResult checkOut(CheckOutCommand command);
}
