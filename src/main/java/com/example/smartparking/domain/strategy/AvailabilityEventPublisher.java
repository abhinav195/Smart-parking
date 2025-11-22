package com.example.smartparking.domain.strategy;

import java.time.Instant;
import java.util.UUID;

public interface AvailabilityEventPublisher {
    enum EventType { SPOT_RESERVED, SPOT_RELEASED, SPOT_OCCUPIED, SPOT_OUT_OF_SERVICE}

    record AvailabilityEvent(
            EventType type,
            UUID lotId,
            UUID floorId,
            UUID spotId,
            String spotCode,
            String spotSize,
            String spotStatus,
            Instant occuredAt
    ){}

    void publish(AvailabilityEvent event);
}
