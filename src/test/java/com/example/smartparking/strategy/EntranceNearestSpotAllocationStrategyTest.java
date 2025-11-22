package com.example.smartparking.strategy;

import com.example.smartparking.domain.Floor;
import com.example.smartparking.domain.Spot;
import com.example.smartparking.domain.SpotSize;
import com.example.smartparking.domain.SpotStatus;
import com.example.smartparking.domain.strategy.SpotAllocationStrategy.AllocationRequest;
import com.example.smartparking.domain.strategy.SpotAllocationStrategy.AllocationResult;
import com.example.smartparking.repository.FloorRepository;
import com.example.smartparking.repository.SpotRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EntranceNearestSpotAllocationStrategyTest {

    private final FloorRepository floorRepository = mock(FloorRepository.class);
    private final SpotRepository spotRepository = mock(SpotRepository.class);

    private final EntranceNearestSpotAllocationStrategy strategy =
            new EntranceNearestSpotAllocationStrategy(floorRepository, spotRepository);

    @Test
    void allocate_returns_first_available_spot_across_floors() {
        UUID lotId = UUID.randomUUID();
        UUID entranceId = UUID.randomUUID();
        AllocationRequest req = new AllocationRequest(
                lotId,
                entranceId,
                SpotSize.MEDIUM,
                Optional.empty(),
                Instant.now()
        );

        Floor floor1 = new Floor(UUID.randomUUID(), lotId, "G", 1);
        Floor floor2 = new Floor(UUID.randomUUID(), lotId, "B1", 2);

        Spot spotOnFloor2 = new Spot(
                UUID.randomUUID(), floor2.getId(), "B1-01", SpotSize.MEDIUM, SpotStatus.AVAILABLE
        );

        when(floorRepository.findByLotIdOrderByOrderingAsc(lotId))
                .thenReturn(List.of(floor1, floor2));

        when(spotRepository.findFirstByFloorIdAndSizeAndStatusOrderByCodeAsc(
                floor1.getId(), SpotSize.MEDIUM, SpotStatus.AVAILABLE))
                .thenReturn(Optional.empty());

        when(spotRepository.findFirstByFloorIdAndSizeAndStatusOrderByCodeAsc(
                floor2.getId(), SpotSize.MEDIUM, SpotStatus.AVAILABLE))
                .thenReturn(Optional.of(spotOnFloor2));

        Optional<AllocationResult> resOpt = strategy.allocateSpot(req);
        assertTrue(resOpt.isPresent());
        AllocationResult res = resOpt.get();

        assertEquals(spotOnFloor2.getId(), res.spotId());
        assertEquals(floor2.getId(), res.floorId());
        assertFalse(res.reservedSpot());
        assertEquals("nearest_by_floor_order", res.reason());
    }

    @Test
    void allocate_returns_empty_when_no_spot_available() {
        UUID lotId = UUID.randomUUID();
        AllocationRequest req = new AllocationRequest(
                lotId,
                UUID.randomUUID(),
                SpotSize.SMALL,
                Optional.empty(),
                Instant.now()
        );

        Floor floor1 = new Floor(UUID.randomUUID(), lotId, "G", 1);

        when(floorRepository.findByLotIdOrderByOrderingAsc(lotId))
                .thenReturn(List.of(floor1));

        when(spotRepository.findFirstByFloorIdAndSizeAndStatusOrderByCodeAsc(
                floor1.getId(), SpotSize.SMALL, SpotStatus.AVAILABLE))
                .thenReturn(Optional.empty());

        Optional<AllocationResult> resOpt = strategy.allocateSpot(req);
        assertTrue(resOpt.isEmpty());
    }
}