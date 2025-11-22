package com.example.smartparking.strategy;

import com.example.smartparking.domain.strategy.FeeCalculationStrategy.FeeBreakdown;
import com.example.smartparking.domain.strategy.FeeCalculationStrategy.FeeRequest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DegressiveDayNightWeekendFeeStrategyTest {

    private final DegressiveDayNightWeekendFeeStrategy strategy =
            new DegressiveDayNightWeekendFeeStrategy();

    @Test
    void grace_period_is_free() {
        Instant entry = Instant.parse("2025-01-01T10:00:00Z");
        Instant exit  = entry.plusSeconds(9 * 60); // 9 minutes

        FeeBreakdown fee = strategy.calculate(new FeeRequest(
                UUID.randomUUID(), UUID.randomUUID(), entry, exit, "INR"));

        assertEquals(0L, fee.totalAmountMinor());
        assertEquals("within_grace_period", fee.description());
    }

    @Test
    void one_hour_daytime_weekday_uses_first_hour_rate() {
        Instant entry = Instant.parse("2025-01-01T10:00:00Z"); // Wednesday
        Instant exit  = entry.plusSeconds(70 * 60); // 1h10m total, 1h after grace

        FeeBreakdown fee = strategy.calculate(new FeeRequest(
                UUID.randomUUID(), UUID.randomUUID(), entry, exit, "INR"));

        assertTrue(fee.totalAmountMinor() > 0L);
        assertTrue(fee.description().startsWith("degressive_day_night_weekend"));
    }

    @Test
    void long_stay_triggers_penalty() {
        Instant entry = Instant.parse("2025-01-01T10:00:00Z");
        // 13 hours later
        Instant exit  = entry.plusSeconds(13 * 60 * 60);

        FeeBreakdown fee = strategy.calculate(new FeeRequest(
                UUID.randomUUID(), UUID.randomUUID(), entry, exit, "INR"));

        assertTrue(fee.penaltyMinor() > 0);
        assertTrue(fee.totalAmountMinor() >= fee.penaltyMinor());
    }
}