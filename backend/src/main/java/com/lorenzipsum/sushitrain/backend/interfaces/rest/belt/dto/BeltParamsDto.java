package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto;

import java.util.UUID;

public record BeltParamsDto(UUID id, Integer tickIntervalMs, Integer speedSlotsPerTick) {
}
