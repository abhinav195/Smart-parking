package com.example.smartparking.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ParkingMetrics {

    private final Counter checkInCounter;
    private final Counter checkOutCounter;
    private final Counter checkInConflictCounter;

    public ParkingMetrics(MeterRegistry registry) {
        this.checkInCounter = Counter.builder("parking.checkins.total")
                .description("Total successful parking check-ins")
                .register(registry);

        this.checkOutCounter = Counter.builder("parking.checkouts.total")
                .description("Total successful parking check-outs")
                .register(registry);

        this.checkInConflictCounter = Counter.builder("parking.checkins.conflicts")
                .description("Check-in attempts rejected due to existing OPEN ticket")
                .register(registry);
    }

    public void onCheckInSuccess() {
        checkInCounter.increment();
    }

    public void onCheckOutSuccess() {
        checkOutCounter.increment();
    }

    public void onCheckInConflict() {
        checkInConflictCounter.increment();
    }
}