package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@DataJpaTest
@ActiveProfiles("test")
public abstract class JpaBaseRepositoryIT {

    @Container
    @ServiceConnection
    protected static PostgreSQLContainer db = new PostgreSQLContainer("postgres:18-alpine");

    @Autowired
    protected EntityManager em;
}
