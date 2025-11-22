package com.example.smartparking.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TicketTest {

    @Test
    void new_ticket_is_open() {
        UUID lotId = UUID.randomUUID();
        UUID spotId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        Instant entryAt = Instant.now();

        Ticket ticket = new Ticket(
                UUID.randomUUID(),
                lotId,
                spotId,
                vehicleId,
                entryAt
        );

        assertEquals(TicketStatus.OPEN, ticket.getStatus());
        assertEquals(entryAt, ticket.getEntryAt());
        assertNull(ticket.getExitAt());
    }

    @Test
    void close_sets_exit_and_status_closed() {
        Ticket ticket = new Ticket(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2025-01-01T10:00:00Z")
        );

        Instant exitAt = Instant.parse("2025-01-01T11:00:00Z");

        ticket.close(exitAt);

        assertEquals(TicketStatus.CLOSED, ticket.getStatus());
        assertEquals(exitAt, ticket.getExitAt());
    }

    @Test
    void close_with_exit_before_entry_throws() {
        Ticket ticket = new Ticket(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2025-01-01T10:00:00Z")
        );

        Instant exitAt = Instant.parse("2025-01-01T09:59:59Z");

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> ticket.close(exitAt)
        );

        assertEquals("Exit time must be after entry time", ex.getMessage());
    }

    @Test
    void closing_twice_throws() {
        Ticket ticket = new Ticket(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2025-01-01T10:00:00Z")
        );

        ticket.close(Instant.parse("2025-01-01T11:00:00Z"));

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> ticket.close(Instant.parse("2025-01-01T12:00:00Z"))
        );

        assertEquals("Ticket is already closed", ex.getMessage());
    }
}