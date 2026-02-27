package com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PickPlateRequest(
        @NotNull
        UUID plateId
) {
}
