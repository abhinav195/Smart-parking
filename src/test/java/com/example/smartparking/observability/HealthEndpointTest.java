package com.example.smartparking.observability;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class HealthEndpointTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void health_includes_lot_data() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/actuator/health", String.class);

        assertEquals(200, response.getStatusCode().value());
        String body = response.getBody();
        // basic checks that health is UP and our custom indicator is present
        assertTrue(body.contains("\"status\":\"UP\""));
        assertTrue(body.contains("lotData")); // name based on indicator bean
    }
    @Test
    void metrics_endpoint_exposes_parking_metrics() {
        ResponseEntity<String> list =
                restTemplate.getForEntity("/actuator/metrics", String.class);

        assertEquals(200, list.getStatusCode().value());
        String body = list.getBody();
        assertTrue(body.contains("parking.checkins.total"));
        assertTrue(body.contains("parking.checkouts.total"));
    }
}