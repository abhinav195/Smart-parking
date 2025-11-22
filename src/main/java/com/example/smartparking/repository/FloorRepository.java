package com.example.smartparking.repository;

import com.example.smartparking.domain.Floor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FloorRepository extends JpaRepository<Floor, UUID> {
    List<Floor> findByLotIdOrderByOrderingAsc(UUID lotId);
}
