package com.lorenzipsum.sushitrain.backend.domain.order;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.OrderStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("LombokGetterMayBeUsed")
public class Order {
    private static final String ERR_ORDER_NOT_OPEN = "Order must be OPEN";
    private final UUID id;
    private final UUID seatId;
    private final List<OrderLine> lines;
    private OrderStatus status;
    private final Instant createdAt;
    private Instant closedAt;

    private Order(UUID id, UUID seatId, List<OrderLine> lines, OrderStatus status, Instant createdAt, Instant closedAt) {
        this.id = id;
        this.seatId = seatId;
        this.lines = (lines == null) ? new ArrayList<>() : new ArrayList<>(lines);
        this.status = status;
        this.createdAt = createdAt;
        this.closedAt = closedAt;
    }

    public static Order open(UUID seatId) {
        if (seatId == null) throw new IllegalArgumentException("Seat cannot be null");
        return new Order(UUID.randomUUID(), seatId, new ArrayList<>(), OrderStatus.OPEN, Instant.now(), null);
    }

    public static Order rehydrate(UUID id, UUID seatId, List<OrderLine> lines, OrderStatus status, Instant createdAt, Instant closedAt) {
        return new Order(id, seatId, lines, status, createdAt, closedAt);
    }

    public OrderLine addLineFromPlate(UUID plateId, String menuItemNameSnapshot, PlateTier tierSnapshot, int priceAtPickInYen) {
        if (plateId == null) throw new IllegalArgumentException("Plate cannot be null");
        if (menuItemNameSnapshot == null || menuItemNameSnapshot.isBlank())
            throw new IllegalArgumentException("Name must not be blank");
        if (status != OrderStatus.OPEN) throw new IllegalStateException(ERR_ORDER_NOT_OPEN);
        if (priceAtPickInYen < 0) throw new IllegalArgumentException("Price cannot be a negative value");

        OrderLine orderLine = OrderLine.create(plateId, this.id, menuItemNameSnapshot, tierSnapshot, priceAtPickInYen);
        this.lines.add(orderLine);
        return orderLine;
    }

    public MoneyYen total() {
        int sum = lines.stream().mapToInt(l -> l.getPriceAtPick().amount()).sum();
        return new MoneyYen(sum);
    }

    public void checkout() {
        if (status != OrderStatus.OPEN) {
            throw new IllegalStateException(ERR_ORDER_NOT_OPEN);
        }
        this.status = OrderStatus.CHECKED_OUT;
        this.closedAt = Instant.now();
    }

    public List<OrderLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public void removeLine(OrderLine line) {
        if (status != OrderStatus.OPEN) throw new IllegalStateException(ERR_ORDER_NOT_OPEN);
        if (line == null) return;
        if (!this.id.equals(line.getOrderId())) {
            throw new IllegalArgumentException("OrderLine does not belong to this Order");
        }
        lines.remove(line); // orphanRemoval will delete the row
        line.clearOrder(); // keep both sides consistent
    }

    public UUID getId() {
        return id;
    }

    public UUID getSeatId() {
        return seatId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }
}
