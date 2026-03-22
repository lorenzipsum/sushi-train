package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt;

import java.time.Instant;
import java.util.UUID;

public record BeltUiEvent(
        UUID eventId,
        UUID beltId,
        String type,
        Instant occurredAt
) {
}
