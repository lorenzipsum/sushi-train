package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static com.lorenzipsum.sushitrain.backend.IntegrationTestDatabase.create;

public class JpaRepositoryBase {
    @Container
    @ServiceConnection
    private static final PostgreSQLContainer db = create();
    @Autowired
    TestEntityManager em;
}
