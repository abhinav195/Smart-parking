package com.example.smartparking.application;

import com.example.smartparking.application.ParkingQueryService.*;
import com.example.smartparking.application.mapper.ParkingQueryMapper;
import com.example.smartparking.domain.*;
import com.example.smartparking.repository.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ParkingQueryServiceImplTest {

    private final LotRepository lotRepository = mock(LotRepository.class);
    private final FloorRepository floorRepository = mock(FloorRepository.class);
    private final SpotRepository spotRepository = mock(SpotRepository.class);
    private final VehicleRepository vehicleRepository = mock(VehicleRepository.class);
    private final TicketRepository ticketRepository = mock(TicketRepository.class);
    private final ParkingQueryMapper mapper = new ParkingQueryMapper();

    private final ParkingQueryServiceImpl service =
            new ParkingQueryServiceImpl(
                    lotRepository, floorRepository, spotRepository,
                    vehicleRepository, ticketRepository, mapper);

    @Test
    void listLots_maps_entities_to_summaries() {
        Lot lot = new Lot(UUID.randomUUID(), "Central", "Addr", java.time.ZoneId.systemDefault());
        when(lotRepository.findAll()).thenReturn(List.of(lot));

        List<LotSummary> result = service.listLots();

        assertEquals(1, result.size());
        assertEquals(lot.getId(), result.get(0).id());
        assertEquals("Central", result.get(0).name());
    }

    @Test
    void findActiveTicketByVehicle_empty_when_no_vehicle() {
        UUID lotId = UUID.randomUUID();
        when(vehicleRepository.findByLicensePlate("ABC")).thenReturn(Optional.empty());

        Optional<ActiveTicketSummary> result = service.findActiveTicketByVehicle(lotId, "ABC");

        assertTrue(result.isEmpty());
    }
}