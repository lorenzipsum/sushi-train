package com.lorenzipsum.sushitrain.backend.infrastructure.scheduling;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.plate-expiry")
public record PlateExpiryJobProperties(
        boolean enabled,
        Duration fixedDelay,
        Duration initialDelay
) {
    public PlateExpiryJobProperties {
        if (fixedDelay == null) fixedDelay = Duration.ofSeconds(60);
        if (initialDelay == null) initialDelay = Duration.ofSeconds(30);
    }
}