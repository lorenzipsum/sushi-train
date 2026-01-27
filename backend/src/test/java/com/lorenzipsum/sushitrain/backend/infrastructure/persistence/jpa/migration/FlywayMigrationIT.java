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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertTrue(result.migrationsExecuted >= 2, "Expected at least 2 migrations to run");

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
    @DisplayName("Demo seed places up to 24 plates on Main Belt (only empty target slots)")
    void demo_seed_places_24_plates_on_main_belt() throws Exception {
        Flyway flyway = Flyway.configure()
                .dataSource(db.getJdbcUrl(), db.getUsername(), db.getPassword())
                .locations("classpath:db/migration", "classpath:db/demo")
                .cleanDisabled(false)
                .load();

        flyway.clean();
        flyway.migrate();

        try (Connection c = DriverManager.getConnection(
                db.getJdbcUrl(), db.getUsername(), db.getPassword())) {

            int totalSlotsMainBelt = singleInt(c, """
                    SELECT COUNT(*)
                    FROM belt_slot bs
                    JOIN belt b ON b.id = bs.belt_id
                    WHERE b.name = 'Main Belt'
                    """);

            int occupiedTargets = singleInt(c, """
                    SELECT COUNT(*)
                    FROM belt_slot bs
                    JOIN belt b ON b.id = bs.belt_id
                    WHERE b.name = 'Main Belt'
                      AND bs.plate_id IS NOT NULL
                      AND (bs.position_index % 8) = 0
                    """);

            int occupiedAllMainBelt = singleInt(c, """
                    SELECT COUNT(*)
                    FROM belt_slot bs
                    JOIN belt b ON b.id = bs.belt_id
                    WHERE b.name = 'Main Belt'
                      AND bs.plate_id IS NOT NULL
                    """);

            assertTrue(totalSlotsMainBelt > 0, "Expected Main Belt to have belt slots");
            // On a fresh DB, your script should fill exactly 24 target slots (0,8,...,184)
            assertEquals(24, occupiedTargets, "Expected exactly 24 occupied target slots on Main Belt");

            // Safety: script should not fill any non-target slots
            assertEquals(occupiedTargets, occupiedAllMainBelt,
                    "Expected only target slots to be occupied by the demo seed");
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
