package com.lorenzipsum.sushitrain.backend.application.view;

import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;

public record OrderLineView(
        String menuItemName,
        PlateTier plateTier,
        int price
) {
}
