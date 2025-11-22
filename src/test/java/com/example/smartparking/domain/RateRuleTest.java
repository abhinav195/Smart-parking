package com.example.smartparking.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateRuleTest {

    @Test
    void create_valid_rule() {
        RateRule rule = new RateRule(
                0,
                60,
                1000L,
                RateRule.Unit.MINUTE
        );

        assertEquals(0, rule.getStartMinute());
        assertEquals(60, rule.getEndMinute());
        assertEquals(1000L, rule.getPricePerUnit());
        assertEquals(RateRule.Unit.MINUTE, rule.getUnit());
    }

    @Test
    void end_before_start_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new RateRule(60, 30, 1000L, RateRule.Unit.MINUTE)
        );
        assertTrue(ex.getMessage().contains("endMinute"));
    }

    @Test
    void negative_price_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new RateRule(0, 60, -1L, RateRule.Unit.MINUTE)
        );
        assertTrue(ex.getMessage().contains("pricePerUnit"));
    }
}