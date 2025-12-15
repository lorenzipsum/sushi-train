package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity;

import com.lorenzipsum.sushitrain.backend.domain.common.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
public class OrderEntity {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    private SeatEntity seat;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLineEntity> lines;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    //optimistic concurrency
    @Version
    @SuppressWarnings("unused")
    private long version;

    @SuppressWarnings("unused")
    protected OrderEntity() {
    }

    public OrderEntity(UUID id, SeatEntity seat, List<OrderLineEntity> lines, OrderStatus status, Instant createdAt, Instant closedAt) {
        this.id = id;
        this.seat = seat;
        this.lines = lines;
        this.status = status;
        this.createdAt = createdAt;
        this.closedAt = closedAt;
    }

    public void addLine(OrderLineEntity orderLine) {
        orderLine.setOrder(this);
        this.lines.add(orderLine);
    }

    @PrePersist
    @SuppressWarnings("unused")
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
