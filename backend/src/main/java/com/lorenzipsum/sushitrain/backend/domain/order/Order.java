package com.lorenzipsum.sushitrain.backend.domain.order;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.OrderStatus;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.domain.seat.Seat;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id")
    private final List<OrderLine> lines = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.OPEN;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    //optimistic concurrency
    @Version
    private long version;

    protected Order() {
    }

    private Order(UUID id, Seat seat) {
        this.id = id;
        this.seat = seat;
        this.createdAt = Instant.now();
    }

    public static Order open(Seat seat) {
        return new Order(UUID.randomUUID(), seat);
    }

    public OrderLine addLineFromPlate(Plate plate, int priceAtPickYen) {
        if (status != OrderStatus.OPEN) {
            throw new IllegalStateException("order is not OPEN");
        }
        OrderLine orderLine = OrderLine.create(plate, this, priceAtPickYen);
        this.lines.add(orderLine);
        return orderLine;
    }

    public MoneyYen total() {
        int sum = lines.stream().mapToInt(l -> l.getPriceAtPickYen().getAmount()).sum();
        return new MoneyYen(sum);
    }

    public void checkout() {
        if (status != OrderStatus.OPEN) {
            throw new IllegalStateException("order is not OPEN");
        }
        this.status = OrderStatus.CHECKED_OUT;
        this.closedAt = Instant.now();
    }
}
