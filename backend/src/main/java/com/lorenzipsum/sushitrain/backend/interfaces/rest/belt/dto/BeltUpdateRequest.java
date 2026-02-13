package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import static com.lorenzipsum.sushitrain.backend.domain.belt.Belt.*;

public record BeltUpdateRequest(
        @Min(TICK_INTERVAL_MS_MIN_VALUE)
        @Max(TICK_INTERVAL_MS_MAX_VALUE)
        Integer tickIntervalMs,

        @Min(SPEED_SLOTS_PER_TICK_MIN_VALUE)
        @Max(SPEED_SLOTS_PER_TICK_MAX_VALUE)
        Integer speedSlotsPerTick) {

        @SuppressWarnings("unused")
        @AssertTrue(message = "At least one parameter must be provided for update.")
        public boolean isAnyParamProvided() {
                return tickIntervalMs != null || speedSlotsPerTick != null;
        }
}
