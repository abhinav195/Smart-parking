package com.example.smartparking.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SpotTest {

    @Test
    void compatibility_by_size() {
        Spot evSpot = new Spot(UUID.randomUUID(), UUID.randomUUID(), "EV1", SpotSize.EV, SpotStatus.AVAILABLE);
        Spot bikeSpot = new Spot(UUID.randomUUID(), UUID.randomUUID(), "B1", SpotSize.BIKE, SpotStatus.AVAILABLE);
        Spot smallSpot = new Spot(UUID.randomUUID(), UUID.randomUUID(), "S1", SpotSize.SMALL, SpotStatus.AVAILABLE);
        Spot mediumSpot = new Spot(UUID.randomUUID(), UUID.randomUUID(), "M1", SpotSize.MEDIUM, SpotStatus.AVAILABLE);
        Spot largeSpot = new Spot(UUID.randomUUID(), UUID.randomUUID(), "L1", SpotSize.LARGE, SpotStatus.AVAILABLE);

        assertTrue(evSpot.isCompatible(SpotSize.EV));
        assertTrue(bikeSpot.isCompatible(SpotSize.BIKE));

        assertTrue(largeSpot.isCompatible(SpotSize.LARGE));
        assertFalse(largeSpot.isCompatible(SpotSize.SMALL));
        assertFalse(largeSpot.isCompatible(SpotSize.MEDIUM));

        assertTrue(mediumSpot.isCompatible(SpotSize.MEDIUM));
        assertTrue(mediumSpot.isCompatible(SpotSize.LARGE));
        assertFalse(mediumSpot.isCompatible(SpotSize.SMALL));

        assertTrue(smallSpot.isCompatible(SpotSize.SMALL));
        assertTrue(smallSpot.isCompatible(SpotSize.MEDIUM));
        assertTrue(smallSpot.isCompatible(SpotSize.LARGE));
    }

    @Test
    void valid_status_transitions() {
        Spot spot = new Spot(UUID.randomUUID(), UUID.randomUUID(), "A1", SpotSize.MEDIUM, SpotStatus.AVAILABLE);

        spot.reserve();
        assertEquals(SpotStatus.RESERVED, spot.getStatus());

        spot.occupy();
        assertEquals(SpotStatus.OCCUPIED, spot.getStatus());

        spot.markAvailable();
        assertEquals(SpotStatus.AVAILABLE, spot.getStatus());

        spot.outOfService();
        assertEquals(SpotStatus.OUT_OF_SERVICE, spot.getStatus());
    }

    @Test
    void invalid_occupy_from_out_of_service_throws() {
        Spot spot = new Spot(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "A2",
                SpotSize.MEDIUM,
                SpotStatus.OUT_OF_SERVICE
        );

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                spot::occupy
        );

        assertTrue(ex.getMessage().contains("Cannot occupy"));
    }
}