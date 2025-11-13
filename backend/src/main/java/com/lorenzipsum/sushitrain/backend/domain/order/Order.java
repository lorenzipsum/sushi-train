package com.lorenzipsum.sushitrain.backend.domain.order;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.OrderStatus;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.domain.seat.Seat;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
public class Order {
    private static final String ERR_ORDER_NOT_OPEN = "Order must be OPEN";

    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderLine> lines = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private OrderStatus status = OrderStatus.OPEN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    //optimistic concurrency
    @Version
    @SuppressWarnings("unused")
    private long version;

    @SuppressWarnings("unused")
    protected Order() {
    }

    private Order(UUID id, Seat seat) {
        this.id = id;
        this.seat = seat;
        this.createdAt = Instant.now();
    }

    public static Order open(Seat seat) {
        if (seat == null) throw new IllegalArgumentException("Seat cannot be null");
        return new Order(UUID.randomUUID(), seat);
    }

    public OrderLine addLineFromPlate(Plate plate, String menuItemNameSnapshot, int priceAtPickInYen) {
        if (plate == null) throw new IllegalArgumentException("Plate cannot be null");
        if (menuItemNameSnapshot == null || menuItemNameSnapshot.isBlank())
            throw new IllegalArgumentException("Name must not be blank");
        if (status != OrderStatus.OPEN) throw new IllegalStateException(ERR_ORDER_NOT_OPEN);
        if (priceAtPickInYen < 0) throw new IllegalArgumentException("Price cannot be a negative value");

        OrderLine orderLine = OrderLine.create(plate, this, menuItemNameSnapshot, priceAtPickInYen);
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

    @PrePersist
    @SuppressWarnings("unused")
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public void removeLine(OrderLine line) {
        if (status != OrderStatus.OPEN) throw new IllegalStateException(ERR_ORDER_NOT_OPEN);
        if (line == null) return;
        if (line.getOrder() != this) {
            throw new IllegalArgumentException("OrderLine does not belong to this Order");
        }
        lines.remove(line); // orphanRemoval will delete the row
        line.clearOrder(); // keep both sides consistent
    }
}
