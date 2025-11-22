package com.example.smartparking.repository;

import com.example.smartparking.domain.RateCard;
import com.example.smartparking.domain.SpotSize;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RateCardRepository extends JpaRepository<RateCard, UUID> {

    List<RateCard> findByLotIdOrLotIdIsNull(UUID lotId);

    Optional<RateCard> findFirstByLotIdAndFloorIdAndSizeAndEffectiveFromLessThanEqualAndEffectiveToIsNullOrderByEffectiveFromDesc(
            UUID lotId,
            UUID floorId,
            SpotSize size,
            Instant at);

    Optional<RateCard> findFirstByLotIdAndFloorIdIsNullAndSizeIsNullAndEffectiveFromLessThanEqualAndEffectiveToIsNullOrderByEffectiveFromDesc(
            UUID lotId,
            Instant at);
}