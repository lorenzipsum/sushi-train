package com.lorenzipsum.sushitrain.backend.infrastructure.scheduling;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.data-integrity-repair")
public record DataIntegrityRepairJobProperties(
        boolean enabled,
        Duration fixedDelay,
        Duration initialDelay
) {
    public DataIntegrityRepairJobProperties {
        if (fixedDelay == null) fixedDelay = Duration.ofMinutes(5);
        if (initialDelay == null) initialDelay = Duration.ofSeconds(30);
    }
}
