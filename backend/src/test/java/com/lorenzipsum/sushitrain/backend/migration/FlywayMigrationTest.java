package com.lorenzipsum.sushitrain.backend.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class FlywayMigrationTest {
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:18-alpine")
                    .withDatabaseName("sushitrain")
                    .withUsername("test")
                    .withPassword("test");

    @BeforeAll
    static void start() {
        POSTGRES.start();
    }

    @AfterAll
    static void stop() {
        POSTGRES.stop();
    }

    @Test
    void migrations_apply_cleanly_and_schema_exists() throws Exception {
        Flyway flyway = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .load();

        var result = flyway.migrate();
        // sanity: at least V1 and V2 should have run
        assertTrue(result.migrationsExecuted >= 2, "Expected at least 2 migrations to run");

        // simple smoke check: a few tables exist
        try (Connection c = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
            assertTrue(tableExists(c, "belt"));
            assertTrue(tableExists(c, "seat"));
            assertTrue(tableExists(c, "menu_item"));
            assertTrue(tableExists(c, "plate"));
            assertTrue(tableExists(c, "orders"));
            assertTrue(tableExists(c, "order_line"));
            assertTrue(tableExists(c, "belt_slot"));
        }
    }

    private boolean tableExists(Connection c, String name) throws Exception {
        try (var ps = c.prepareStatement(
                "select 1 from information_schema.tables where table_schema='public' and table_name=?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
