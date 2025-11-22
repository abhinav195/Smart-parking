package com.example.smartparking.domain;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LotTest {

    @Test
    void create_lot_initial_state() {
        UUID id = UUID.randomUUID();
        Lot lot = new Lot(id, "Central Lot", "Addr", ZoneId.of("Asia/Kolkata"));

        assertEquals(id, lot.getId());
        assertEquals("Central Lot", lot.getName());
        assertEquals("Addr", lot.getAddress());
        assertEquals(ZoneId.of("Asia/Kolkata"), lot.getTimezone());
        assertFalse(lot.getMaintenanceMode());
    }

    @Test
    void change_timezone_updates_timezone() {
        Lot lot = new Lot(
                UUID.randomUUID(),
                "Central Lot",
                "Addr",
                ZoneId.of("Asia/Kolkata")
        );

        lot.changeTimezone(ZoneId.of("UTC"));

        assertEquals(ZoneId.of("UTC"), lot.getTimezone());
    }
}