package com.lorenzipsum.sushitrain.backend.application.view;

import java.util.UUID;

public record SeatOrderView(
        UUID seatId,
        String label,
        int positionIndex,
        boolean isOccupied,
        OrderSummaryView orderSummary
) {
}
