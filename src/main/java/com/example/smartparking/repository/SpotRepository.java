package com.example.smartparking.repository;

import com.example.smartparking.domain.Spot;
import com.example.smartparking.domain.SpotSize;
import com.example.smartparking.domain.SpotStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpotRepository extends JpaRepository<Spot, UUID> {

    List<Spot> findByFloorIdAndStatus(UUID floorId, SpotStatus status);
    List<Spot> findByFloorId(UUID floorId);

    List<Spot> findByFloorIdAndSizeAndStatus(
            UUID floorId,
            SpotSize size,
            SpotStatus status
    );
    Optional<Spot> findFirstByFloorIdAndSizeAndStatusOrderByCodeAsc(
            UUID floorId,
            SpotSize size,
            SpotStatus status);
}