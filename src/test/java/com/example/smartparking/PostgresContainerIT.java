package com.example.smartparking;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class PostgresContainerIT {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:18");

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void contextStarts_andFlywayMigrationsApplied() {
        Integer one = jdbcTemplate.queryForObject("select 1", Integer.class);
        Integer count = jdbcTemplate.queryForObject("select count(*) from flyway_schema_history", Integer.class);
    }
}
