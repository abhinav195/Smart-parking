package com.example.smartparking.api;

import com.example.smartparking.AbstractIntegrationTest;
import com.example.smartparking.application.ParkingSessionService.CheckInResult;
import com.example.smartparking.application.ParkingSessionService.CheckOutResult;
import com.example.smartparking.application.ParkingQueryService.ActiveTicketSummary;
import com.example.smartparking.domain.SpotSize;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ParkingSessionApiTest extends AbstractIntegrationTest {

    static class CheckInRequest {
        public UUID lotId;
        public UUID entranceId;
        public String licensePlate;
        public SpotSize vehicleSize;
    }

    static class CheckOutRequest {
        public UUID lotId;
        public UUID ticketId;
    }

    @Test
    void full_flow_check_in_active_ticket_check_out() {
        // given seeded data from Flyway: adjust if your IDs differ
        UUID lotId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID entranceId = UUID.fromString("00000000-0000-0000-0000-000000000011");
        String plate = "MH12IT1234";

        // ---- 1) Check-in ----
        CheckInRequest in = new CheckInRequest();
        in.lotId = lotId;
        in.entranceId = entranceId;
        in.licensePlate = plate;
        in.vehicleSize = SpotSize.MEDIUM;

        ResponseEntity<CheckInResult> inResponse =
                restTemplate.postForEntity(url("/api/sessions/check-in"), in, CheckInResult.class);

        assertEquals(HttpStatus.CREATED, inResponse.getStatusCode());
        CheckInResult checkIn = inResponse.getBody();
        assertNotNull(checkIn);
        assertNotNull(checkIn.ticketId());
        assertNotNull(checkIn.spotId());

        UUID ticketId = checkIn.ticketId();

        // ---- 2) Query active ticket ----
        String activeTicketUrl = url("/api/lots/" + lotId + "/active-ticket?licensePlate=" + plate);
        ResponseEntity<ActiveTicketSummary> activeResponse =
                restTemplate.getForEntity(activeTicketUrl, ActiveTicketSummary.class);

        assertEquals(HttpStatus.OK, activeResponse.getStatusCode());
        ActiveTicketSummary active = activeResponse.getBody();
        assertNotNull(active);
        assertEquals(ticketId, active.ticketId());
        assertEquals(checkIn.spotId(), active.spotId());

        // ---- 3) Check-out ----
        CheckOutRequest out = new CheckOutRequest();
        out.lotId = lotId;
        out.ticketId = ticketId;

        ResponseEntity<CheckOutResult> outResponse =
                restTemplate.postForEntity(url("/api/sessions/check-out"), out, CheckOutResult.class);

        assertEquals(HttpStatus.OK, outResponse.getStatusCode());
        CheckOutResult checkOut = outResponse.getBody();
        assertNotNull(checkOut);
        assertEquals(ticketId, checkOut.ticketId());
        assertTrue(checkOut.amountMinor() >= 0);

        // ---- 4) After check-out: active ticket should be gone ----
        ResponseEntity<ActiveTicketSummary> activeAfter =
                restTemplate.getForEntity(activeTicketUrl, ActiveTicketSummary.class);

        // empty Optional maps to 200 with null body by default
        assertEquals(HttpStatus.OK, activeAfter.getStatusCode());
        assertNull(activeAfter.getBody());
    }
}