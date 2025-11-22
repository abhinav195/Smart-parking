package com.example.smartparking.api;

import com.example.smartparking.application.ParkingQueryService;
import com.example.smartparking.application.ParkingQueryService.*;
import com.example.smartparking.domain.SpotSize;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ParkingQueryController {

    private final ParkingQueryService parkingQueryService;

    public ParkingQueryController(ParkingQueryService parkingQueryService) {
        this.parkingQueryService = parkingQueryService;
    }

    @GetMapping("/lots")
    public List<LotSummary> listLots() {
        return parkingQueryService.listLots();
    }

    @GetMapping("/lots/{lotId}/floors")
    public List<FloorSummary> listFloors(@PathVariable UUID lotId) {
        return parkingQueryService.listFloors(lotId);
    }

    @GetMapping("/floors/{floorId}/spots")
    public List<SpotSummary> listSpotsByFloor(@PathVariable UUID floorId) {
        return parkingQueryService.listSpotsByFloor(floorId);
    }

    @GetMapping("/floors/{floorId}/available")
    public List<SpotSummary> listAvailableByFloorAndSize(
            @PathVariable UUID floorId,
            @RequestParam("size") SpotSize size
    ) {
        return parkingQueryService.listAvailableSpotsByFloor(floorId, size);
    }

    @GetMapping("/lots/{lotId}/active-ticket")
    public Optional<ActiveTicketSummary> findActiveTicket(
            @PathVariable UUID lotId,
            @RequestParam("licensePlate")
            @Pattern(regexp = "^[A-Z0-9\\- ]{3,20}$",
                    message = "licensePlate must be 3-20 chars, uppercase letters, digits, space or dash")
            String licensePlate
    ) {
        return parkingQueryService.findActiveTicketByVehicle(lotId, licensePlate);
    }
}