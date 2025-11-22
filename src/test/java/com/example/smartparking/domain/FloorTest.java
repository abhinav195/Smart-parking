package com.example.smartparking.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FloorTest {

    @Test
    void create_floor_sets_fields() {
        UUID id = UUID.randomUUID();
        UUID lotId = UUID.randomUUID();

        Floor floor = new Floor(id, lotId, "G", 1);

        assertEquals(id, floor.getId());
        assertEquals(lotId, floor.getLotId());
        assertEquals("G", floor.getLabel());
        assertEquals(1, floor.getOrdering());
    }
}