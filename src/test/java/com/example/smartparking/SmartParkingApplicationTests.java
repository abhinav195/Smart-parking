package com.example.smartparking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class SmartParkingApplicationTests {

	@Test
	void contextLoads() {
		// just verifies that the Spring context starts
	}
}