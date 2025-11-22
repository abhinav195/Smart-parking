package com.example.smartparking.domain.strategy;

import java.time.Instant;
import java.util.UUID;

public interface FeeCalculationStrategy {
    record FeeRequest(
            UUID id,
            UUID ticketId,
            Instant entryAt,
            Instant exitAt,
            String currency
    ){}
    record FeeBreakdown(
            long baseAmountMinor,
            long degressiveAmountMinor,
            long penaltyMinor,
            long totalAmountMinor,
            String description
    ){}
    FeeBreakdown calculate(FeeRequest feeRequest);
}
