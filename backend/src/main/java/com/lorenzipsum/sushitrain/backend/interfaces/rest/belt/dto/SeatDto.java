package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto;

import java.util.UUID;

public record SeatDto(
        UUID id,
        Integer positionIndex,
        String label) {
}
