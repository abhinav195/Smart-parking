package com.example.smartparking.api;

import com.example.smartparking.application.ParkingSessionService;
import com.example.smartparking.application.ParkingSessionService.CheckInCommand;
import com.example.smartparking.application.ParkingSessionService.CheckInResult;
import com.example.smartparking.application.ParkingSessionService.CheckOutCommand;
import com.example.smartparking.application.ParkingSessionService.CheckOutResult;
import com.example.smartparking.domain.SpotSize;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ParkingSessionController {

    private final ParkingSessionService parkingSessionService;

    public ParkingSessionController(ParkingSessionService parkingSessionService) {
        this.parkingSessionService = parkingSessionService;
    }

    // ---------- Check-in ----------

    public static final class CheckInRequest {
        @NotNull
        public UUID lotId;
        @NotNull
        public UUID entranceId;
        @NotNull
        @Pattern(regexp = "^[A-Z0-9\\- ]{3,20}$",
                message = "licensePlate must be 3-20 chars, uppercase letters, digits, space or dash")
        public String licensePlate;
        public SpotSize vehicleSize;
        @NotNull
        public UUID reservationId;
    }

    @PostMapping("/sessions/check-in")
    @ResponseStatus(HttpStatus.CREATED)
    public CheckInResult checkIn(@RequestBody CheckInRequest body) {
        CheckInCommand command = new CheckInCommand(
                body.lotId,
                body.entranceId,
                body.licensePlate,
                body.vehicleSize,
                Optional.ofNullable(body.reservationId),
                Instant.now()
        );
        return parkingSessionService.checkIn(command);
    }

    // ---------- Check-out ----------

    public static final class CheckOutRequest {
        @NotNull
        public UUID lotId;
        @NotNull
        public UUID ticketId;
    }

    @PostMapping("/sessions/check-out")
    public CheckOutResult checkOut(@RequestBody CheckOutRequest body) {
        CheckOutCommand command = new CheckOutCommand(
                body.lotId,
                body.ticketId,
                Instant.now()
        );
        return parkingSessionService.checkOut(command);
    }
}