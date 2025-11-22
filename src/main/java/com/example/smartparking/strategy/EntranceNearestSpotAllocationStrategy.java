package com.example.smartparking.strategy;

import com.example.smartparking.domain.Floor;
import com.example.smartparking.domain.Spot;
import com.example.smartparking.domain.SpotSize;
import com.example.smartparking.domain.SpotStatus;
import com.example.smartparking.domain.strategy.SpotAllocationStrategy;
import com.example.smartparking.repository.FloorRepository;
import com.example.smartparking.repository.SpotRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Entrance-based, nearest-first allocation.
 * For now, we scan floors in their configured ordering;
 */
@Service
public class EntranceNearestSpotAllocationStrategy implements SpotAllocationStrategy {

    private final FloorRepository floorRepository;
    private final SpotRepository spotRepository;

    public EntranceNearestSpotAllocationStrategy(
            FloorRepository floorRepository,
            SpotRepository spotRepository
    ) {
        this.floorRepository = floorRepository;
        this.spotRepository = spotRepository;
    }

    public Optional<AllocationResult> allocateSpot(AllocationRequest request) {
        UUID lotId = request.lotId();
        SpotSize vehicleSize = request.vehicleSize();

        List<Floor> floors = floorRepository.findByLotIdOrderByOrderingAsc(lotId);

        for (Floor floor : floors) {
            Optional<Spot> candidate =
                    spotRepository.findFirstByFloorIdAndSizeAndStatusOrderByCodeAsc(
                            floor.getId(),
                            vehicleSize,
                            SpotStatus.AVAILABLE
                    );

            if (candidate.isPresent()) {
                Spot spot = candidate.get();
                return Optional.of(
                        new AllocationResult(
                                spot.getId(),
                                floor.getId(),
                                false,                      // reservedSpot
                                "nearest_by_floor_order"    // reason
                        )
                );
            }
        }

        // No suitable spot found
        return Optional.empty();
    }
}