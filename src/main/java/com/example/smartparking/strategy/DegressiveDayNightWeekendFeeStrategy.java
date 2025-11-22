package com.example.smartparking.strategy;

import com.example.smartparking.domain.strategy.FeeCalculationStrategy;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;

/**
 * Degressive pricing with day/night and weekday/weekend multipliers,
 * plus a grace period and overstay penalty.
 */
@Service
public class DegressiveDayNightWeekendFeeStrategy implements FeeCalculationStrategy {

    // Amounts are in minor units (e.g., paise for INR)
    private static final long FIRST_HOUR_RATE = 4000L; // 40.00
    private static final long MID_HOURS_RATE  = 2500L; // 25.00 (1-4 hours)
    private static final long LONG_STAY_RATE  = 1500L; // 15.00 (4+ hours)

    private static final int GRACE_MINUTES = 10;
    private static final long OVERSTAY_THRESHOLD_MINUTES = 12 * 60L;
    private static final long OVERSTAY_PENALTY_MINOR = 20000L; // 200.00

    private static final LocalTime DAY_START = LocalTime.of(8, 0);
    private static final LocalTime DAY_END   = LocalTime.of(20, 0);

    @Override
    public FeeBreakdown calculate(FeeRequest request) {
        Instant entry = request.entryAt();
        Instant exit  = request.exitAt();
        if (exit.isBefore(entry)) {
            throw new IllegalArgumentException("exitAt must not be before entryAt");
        }

        long totalMinutes = ChronoUnit.MINUTES.between(entry, exit);

        // Grace period
        if (totalMinutes <= GRACE_MINUTES) {
            return new FeeBreakdown(
                    0L,  // base
                    0L,  // degressiveDiscountMinor
                    0L,  // penaltyMinor
                    0L,
                    "within_grace_period"
            );
        }

        long billableMinutes = totalMinutes - GRACE_MINUTES;

        // Convert to hours, rounded up
        long billableHours = Math.max(1L, (billableMinutes + 59) / 60);

        // Degressive base without multipliers
        long baseWithoutMultipliers = computeDegressiveBase(billableHours);

        // Day/night + weekday/weekend multipliers based on entry time
        double dayNightFactor = isDay(entry) ? 1.0 : 0.8;
        double weekdayWeekendFactor = isWeekend(entry) ? 1.2 : 1.0;

        long baseWithMultipliers = Math.round(
                baseWithoutMultipliers * dayNightFactor * weekdayWeekendFactor
        );

        long penalty = (billableMinutes > OVERSTAY_THRESHOLD_MINUTES)
                ? OVERSTAY_PENALTY_MINOR
                : 0L;

        long total = baseWithMultipliers + penalty;

        long discount = Math.max(0L, baseWithoutMultipliers - baseWithMultipliers);

        String description = "degressive_day_night_weekend"
                + (penalty > 0 ? "_with_overstay_penalty" : "");

        return new FeeBreakdown(
                baseWithMultipliers,
                discount,
                penalty,
                total,
                description
        );
    }

    private long computeDegressiveBase(long hours) {
        long remaining = hours;
        long total = 0;

        // First hour (max 1h)
        if (remaining > 0) {
            long block = Math.min(1, remaining);
            total += block * FIRST_HOUR_RATE;
            remaining -= block;
        }

        // Next 3 hours (1-4h)
        if (remaining > 0) {
            long block = Math.min(3, remaining);
            total += block * MID_HOURS_RATE;
            remaining -= block;
        }

        // Remaining hours (4+)
        if (remaining > 0) {
            total += remaining * LONG_STAY_RATE;
        }

        return total;
    }

    private boolean isDay(Instant instant) {
        ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
        LocalTime time = zdt.toLocalTime();
        return !time.isBefore(DAY_START) && time.isBefore(DAY_END);
    }

    private boolean isWeekend(Instant instant) {
        DayOfWeek dow = instant.atZone(ZoneId.systemDefault()).getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }
}