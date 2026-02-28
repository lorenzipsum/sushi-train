package com.lorenzipsum.sushitrain.backend.application.view;

import com.lorenzipsum.sushitrain.backend.domain.common.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderSummaryView(
        UUID orderId,
        UUID seatId,
        OrderStatus status,
        Instant createdAt,
        Instant closedAt,
        List<OrderLineView> lines,
        int totalPrice
) {
}
