package com.example.smartparking.application.mapper;

import com.example.smartparking.domain.Spot;
import com.example.smartparking.domain.strategy.AvailabilityEventPublisher.AvailabilityEvent;
import com.example.smartparking.domain.strategy.AvailabilityEventPublisher.EventType;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class AvailabilityEventMapper {

    public AvailabilityEvent toEvent(
            EventType type,
            UUID lotId,
            UUID floorId,
            Spot spot,
            Instant occurredAt
    ) {
        return new AvailabilityEvent(
                type,
                lotId,
                floorId,
                spot.getId(),
                spot.getCode(),
                spot.getSize().name(),
                spot.getStatus().name(),
                occurredAt
        );
    }
}