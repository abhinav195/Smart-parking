package com.example.smartparking.observability;

import com.example.smartparking.application.ParkingSessionService;
import com.example.smartparking.application.ParkingSessionService.CheckInCommand;
import com.example.smartparking.application.ParkingSessionService.CheckOutCommand;
import com.example.smartparking.domain.SpotSize;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("local")
class ParkingMetricsServiceTest {

    @Autowired
    ParkingSessionService parkingSessionService;

    @Autowired
    MeterRegistry meterRegistry;

    @Test
    void checkin_and_checkout_increment_metrics() {
        // given seeded lot/entrance ids
        UUID lotId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID entranceId = UUID.fromString("00000000-0000-0000-0000-000000000011");
        String plate = "MH12IT-METRICS";

        double beforeCheckIns =
                meterRegistry.counter("parking.checkins.total").count();
        double beforeCheckOuts =
                meterRegistry.counter("parking.checkouts.total").count();

        // when: check-in
        var checkInResult = parkingSessionService.checkIn(
                new CheckInCommand(
                        lotId,
                        entranceId,
                        plate,
                        SpotSize.MEDIUM,
                        Optional.empty(),
                        Instant.now()
                )
        );

        // and: check-out
        parkingSessionService.checkOut(
                new CheckOutCommand(
                        lotId,
                        checkInResult.ticketId(),
                        Instant.now()
                )
        );

        double afterCheckIns =
                meterRegistry.counter("parking.checkins.total").count();
        double afterCheckOuts =
                meterRegistry.counter("parking.checkouts.total").count();

        assertEquals(beforeCheckIns + 1.0, afterCheckIns);
        assertEquals(beforeCheckOuts + 1.0, afterCheckOuts);
    }
}