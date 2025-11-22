package com.example.smartparking.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {

    @Test
    void new_payment_is_pending() {
        UUID ticketId = UUID.randomUUID();
        Payment payment = new Payment(
                UUID.randomUUID(),
                ticketId,
                1500L,
                "INR",
                PaymentMethod.CASH
        );

        assertEquals(ticketId, payment.getTicketId());
        assertEquals(1500L, payment.getAmountMinor());
        assertEquals("INR", payment.getCurrency());
        assertEquals(PaymentStatus.INITIATED, payment.getStatus());
        assertNull(payment.getPaidAt());
    }

    @Test
    void succeed_sets_status_and_reference() {
        Payment payment = new Payment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                1500L,
                "INR",
                PaymentMethod.CARD
        );

        Instant paidAt = Instant.parse("2025-01-01T12:00:00Z");
        payment.succeed("PAY-123", paidAt);

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals("PAY-123", payment.getReference());
        assertEquals(paidAt, payment.getPaidAt());
    }

    @Test
    void fail_sets_status_failed() {
        Payment payment = new Payment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                1500L,
                "INR",
                PaymentMethod.CARD
        );

        payment.fail();

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
    }
}