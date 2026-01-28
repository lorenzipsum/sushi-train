package com.lorenzipsum.sushitrain.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class SushiTrainBackendApplicationTests {

    @Container
    @ServiceConnection
    @SuppressWarnings("unused")
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:18-alpine");

    @Test
    void contextLoads() {
    }

}
