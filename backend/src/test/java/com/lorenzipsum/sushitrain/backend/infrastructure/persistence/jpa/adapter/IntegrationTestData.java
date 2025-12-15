package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Duration;

public class IntegrationTestData {

    @SuppressWarnings("resource")
    public static PostgreSQLContainer createDb() {
        return new PostgreSQLContainer("postgres:18-alpine")
                .withDatabaseName("sushitrain")
                .withUsername("sushi")
                .withPassword("sushi")
                .withStartupTimeout(Duration.ofSeconds(60));
    }

    public static void registerDynamicProperties(DynamicPropertyRegistry r, PostgreSQLContainer DB) {
        r.add("spring.datasource.url", DB::getJdbcUrl);
        r.add("spring.datasource.username", DB::getUsername);
        r.add("spring.datasource.password", DB::getPassword);
        // prefer Flyway-managed schema
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.flyway.locations", () -> "classpath:db/migration");
    }
}
