package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import static com.lorenzipsum.sushitrain.backend.domain.belt.Belt.*;

@Schema(
        name = "BeltUpdateRequest",
        description = "Partial update for belt settings. Omit a field (null) to keep its current value. At least one field must be provided."
)
public record BeltUpdateRequest(

        @Schema(
                description = "Tick interval in milliseconds. Lower = more frequent ticks (potentially higher load). " +
                        "Omit to keep current value.",
                example = "750",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                minimum = "" + TICK_INTERVAL_MS_MIN_VALUE,
                maximum = "" + TICK_INTERVAL_MS_MAX_VALUE
        )
        @Min(TICK_INTERVAL_MS_MIN_VALUE)
        @Max(TICK_INTERVAL_MS_MAX_VALUE)
        Integer tickIntervalMs,

        @Schema(
                description = "Belt speed measured as slots moved per tick. Higher = faster belt. " +
                        "Omit to keep current value.",
                example = "2",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                minimum = "" + SPEED_SLOTS_PER_TICK_MIN_VALUE,
                maximum = "" + SPEED_SLOTS_PER_TICK_MAX_VALUE
        )
        @Min(SPEED_SLOTS_PER_TICK_MIN_VALUE)
        @Max(SPEED_SLOTS_PER_TICK_MAX_VALUE)
        Integer speedSlotsPerTick
) {

        @SuppressWarnings("unused")
        @AssertTrue(message = "At least one parameter must be provided.")
        @JsonIgnore
        @Schema(hidden = true)
        public boolean hasAnyParamProvided() {
                return tickIntervalMs != null || speedSlotsPerTick != null;
        }
}
