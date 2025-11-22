package com.example.smartparking.domain.strategy;

import com.example.smartparking.domain.SpotSize;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SpotAllocationStrategy {
    record AllocationRequest(
            UUID lotId,
            UUID entranceId,
            SpotSize vehicleSize,
            Optional<UUID> reservationId,
            Instant checkInTime
    ) {}
    record AllocationResult(
            UUID spotId,
            UUID floorId,
            boolean reservedSpot,
            String reason
    ){}
    Optional<AllocationResult> allocateSpot(AllocationRequest allocationRequest);
}
