package com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto;

import com.lorenzipsum.sushitrain.backend.domain.common.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderSummaryDto(
        UUID orderId,
        UUID seatId,
        OrderStatus status,
        Instant createdAt,
        Instant closedAt,
        List<OrderLineDto> lines,
        int totalPrice
) {
}
