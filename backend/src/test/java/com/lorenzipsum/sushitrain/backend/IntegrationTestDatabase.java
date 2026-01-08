package com.lorenzipsum.sushitrain.backend;

import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Duration;

public class IntegrationTestDatabase {

    public static final String POSTGRES_18_ALPINE = "postgres:18-alpine";

    @SuppressWarnings("resource")
    public static PostgreSQLContainer create() {
        return new PostgreSQLContainer(POSTGRES_18_ALPINE)
                .withDatabaseName("sushitrain")
                .withUsername("sushi")
                .withPassword("sushi")
                .withStartupTimeout(Duration.ofSeconds(60));
    }
}
