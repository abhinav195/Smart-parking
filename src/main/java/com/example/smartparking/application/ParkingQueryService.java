package com.example.smartparking.application;

import com.example.smartparking.domain.SpotSize;
import com.example.smartparking.domain.SpotStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Read-side application service for querying lots, floors, and spots.
public interface ParkingQueryService {
    record LotSummary(
            UUID id,
            String name,
            String address
    ){}
    record FloorSummary(
            UUID floorId,
            String label,
            int ordering
    ){}
    record SpotSummary(
            UUID spotId,
            UUID floorId,
            String code,
            SpotSize size,
            SpotStatus status
    ){}
    record ActiveTicketSummary(
            UUID ticketId,
            UUID lotId,
            UUID spotId,
            String spotCode,
            SpotSize spotSize
    ){}
    List<LotSummary> listLots();
    List<FloorSummary> listFloors(UUID lotId);
    List<SpotSummary> listSpotsByFloor(UUID floorId);
    List<SpotSummary> listAvailableSpotsByFloor(UUID floorId, SpotSize size);
    Optional<ActiveTicketSummary> findActiveTicketByVehicle(UUID lotId, String licensePlate);
}
