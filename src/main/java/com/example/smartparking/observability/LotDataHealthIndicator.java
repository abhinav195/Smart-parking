package com.example.smartparking.observability;

import com.example.smartparking.repository.LotRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class LotDataHealthIndicator implements HealthIndicator {

    private final LotRepository lotRepository;

    public LotDataHealthIndicator(LotRepository lotRepository) {
        this.lotRepository = lotRepository;
    }

    @Override
    public Health health() {
        long count = lotRepository.count();
        if (count > 0) {
            return Health.up()
                    .withDetail("lots", count)
                    .build();
        }
        return Health.down()
                .withDetail("lots", 0)
                .withDetail("reason", "no seeded lots found")
                .build();
    }
}