package com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto;

import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;

public record OrderLineDto(
        String menuItemName,
        PlateTier plateTier,
        int price
) {
}
