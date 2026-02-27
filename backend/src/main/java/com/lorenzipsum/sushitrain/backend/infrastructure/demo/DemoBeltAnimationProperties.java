package com.lorenzipsum.sushitrain.backend.infrastructure.demo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.demo-belt-animation")
@SuppressWarnings("unused")
public class DemoBeltAnimationProperties {
    private boolean enabled = true;
    private UUID beltId;
    private boolean clearScreen = true;
}
