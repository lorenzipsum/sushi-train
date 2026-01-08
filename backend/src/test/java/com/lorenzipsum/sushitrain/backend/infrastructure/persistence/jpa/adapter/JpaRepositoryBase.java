package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static com.lorenzipsum.sushitrain.backend.IntegrationTestDatabase.create;

public class JpaRepositoryBase {
    @Container
    static final PostgreSQLContainer db = create();
    @Autowired
    TestEntityManager em;

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry r) {
        registerDynamicProperties(r, db);
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
