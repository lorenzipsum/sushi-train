package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PostgreSqlContainerIT extends JpaBaseRepositoryIT {

    @Test
    void testContainer_connectionEstablished() {
        assertThat(db.isCreated()).isTrue();
        assertThat(db.isRunning()).isTrue();
    }
}
