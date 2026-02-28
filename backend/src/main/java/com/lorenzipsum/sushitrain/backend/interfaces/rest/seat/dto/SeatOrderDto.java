package com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto;

import com.lorenzipsum.sushitrain.backend.interfaces.rest.order.dto.OrderSummaryDto;

import java.util.UUID;

public record SeatOrderDto(
        UUID seatId,
        String label,
        int positionIndex,
        boolean isOccupied,
        OrderSummaryDto orderSummary
) {
}
