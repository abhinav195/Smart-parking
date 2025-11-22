package com.example.smartparking.strategy;

import com.example.smartparking.domain.strategy.AvailabilityEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingAvailabilityEventPublisher implements AvailabilityEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(LoggingAvailabilityEventPublisher.class);

    @Override
    public void publish(AvailabilityEvent event) {
        log.info("Availability event: type={} lot={} floor={} spot={} status={}",
                event.type(),
                event.lotId(),
                event.floorId(),
                event.spotId(),
                event.spotStatus());
    }
}