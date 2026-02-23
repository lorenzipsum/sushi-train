package com.lorenzipsum.sushitrain.backend.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.belt.placement")
public record BeltPlacementProperties(
        int minEmptySlotsBetweenNewPlates,
        int maxNewPlatesPerRequest
) {
    public BeltPlacementProperties {
        if (minEmptySlotsBetweenNewPlates < 0) {
            throw new IllegalArgumentException("minEmptySlotsBetweenNewPlates must be >= 0");
        }
        if (maxNewPlatesPerRequest < 1) {
            throw new IllegalArgumentException("maxNewPlatesPerRequest must be >= 1");
        }
    }
}