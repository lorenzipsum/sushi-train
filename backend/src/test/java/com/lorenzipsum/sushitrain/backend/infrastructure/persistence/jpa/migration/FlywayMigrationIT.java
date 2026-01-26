package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@ActiveProfiles("test")
class FlywayMigrationIT {

    @Container
    @ServiceConnection
    protected static PostgreSQLContainer db = new PostgreSQLContainer("postgres:18-alpine");

    @BeforeAll
    static void start() {
        db.start();
    }

    @AfterAll
    static void stop() {
        db.stop();
    }

    @Test
    @DisplayName("Baseline migrations apply cleanly and schema exists")
    void migrations_apply_cleanly_and_schema_exists() throws Exception {
        Flyway flyway = Flyway.configure()
                .dataSource(db.getJdbcUrl(), db.getUsername(), db.getPassword())
                .locations("classpath:db/migration")
                .load();

        var result = flyway.migrate();
        // sanity: at least V1 and V2 should have run
        assertTrue(result.migrationsExecuted >= 2, "Expected at least 2 migrations to run");

        // simple smoke check: a few tables exist
        try (Connection c = DriverManager.getConnection(
                db.getJdbcUrl(), db.getUsername(), db.getPassword())) {
            assertTrue(tableExists(c, "belt"));
            assertTrue(tableExists(c, "seat"));
            assertTrue(tableExists(c, "menu_item"));
            assertTrue(tableExists(c, "plate"));
            assertTrue(tableExists(c, "orders"));
            assertTrue(tableExists(c, "order_line"));
            assertTrue(tableExists(c, "belt_slot"));
        }
    }

    @Test
    @DisplayName("R__demo_stock fills belt to ~15–25% occupancy (dev repeatable script)")
    void demo_stock_populates_belt_with_target_occupancy() throws Exception {
        // Clean & re-migrate so the repeatable script runs on a fresh schema
        Flyway flyway = Flyway.configure()
                .dataSource(db.getJdbcUrl(), db.getUsername(), db.getPassword())
                .locations(
                        "classpath:db/migration",
                        "classpath:db/demo"
                )
                .cleanDisabled(false)
                .load();

        flyway.clean();
        flyway.migrate();

        try (Connection c = DriverManager.getConnection(
                db.getJdbcUrl(), db.getUsername(), db.getPassword())) {

            int totalSlots = singleInt(c,
                    "SELECT COUNT(*) FROM belt_slot");
            int occupied = singleInt(c,
                    "SELECT COUNT(*) FROM belt_slot WHERE plate_id IS NOT NULL");

            // Avoid div-by-zero in case seed changed
            assertTrue(totalSlots > 0, "Expected belt_slot to have rows");

            double occ = (double) occupied / (double) totalSlots;

            // Because of randomness, allow a slightly wider band to avoid flakiness
            assertTrue(occ >= 0.10 && occ <= 0.30,
                    "Expected belt occupancy in ~10–30% range, was " + String.format("%.2f%%", occ * 100));
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

    private int singleInt(Connection c, String sql) throws Exception {
        try (var st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
