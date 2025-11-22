package com.example.smartparking.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RateCardTest {

    @Test
    void create_rate_card_basic_fields() {
        UUID id = UUID.randomUUID();
        Instant from = Instant.parse("2025-01-01T00:00:00Z");
        Instant to = Instant.parse("2025-12-31T23:59:59Z");

        RateCard card = new RateCard(
                id,
                "Default",
                "INR",
                from,
                to
        );

        assertEquals(id, card.getId());
        assertEquals("Default", card.getName());
        assertEquals("INR", card.getCurrency());
        assertEquals(from, card.getEffectiveFrom());
        assertEquals(to, card.getEffectiveTo());
    }
}