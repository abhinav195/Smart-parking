package com.example.smartparking.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VehicleTest {

    @Test
    void create_vehicle_sets_fields() {
        UUID id = UUID.randomUUID();
        Vehicle vehicle = new Vehicle(id, "MH12AB1234", SpotSize.MEDIUM);

        assertEquals(id, vehicle.getId());
        assertEquals("MH12AB1234", vehicle.getLicensePlate());
        assertEquals(SpotSize.MEDIUM, vehicle.getSize());
    }
}