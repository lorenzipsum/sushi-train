package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_line")
@Getter
public class OrderLineEntity {
    @Id
    private UUID id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plate_id", nullable = false, unique = true)
    private PlateEntity plate;

    @Setter
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "menu_item_name_snapshot", nullable = false, length = 128)
    private String menuItemNameSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_snapshot", nullable = false, length = 16)
    private PlateTier tierSnapshot;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "price_at_pick_yen", nullable = false))
    private MoneyYen priceAtPick;

    @Column(name = "picked_at", nullable = false, updatable = false)
    private Instant pickedAt;

    @SuppressWarnings("unused")
    protected OrderLineEntity() {
    }

    public OrderLineEntity(UUID id, PlateEntity plate, OrderEntity order, String menuItemNameSnapshot, PlateTier tierSnapshot, MoneyYen priceAtPick, Instant pickedAt) {
        this.id = id;
        this.plate = plate;
        this.order = order;
        this.menuItemNameSnapshot = menuItemNameSnapshot;
        this.tierSnapshot = tierSnapshot;
        this.priceAtPick = priceAtPick;
        this.pickedAt = pickedAt;
    }

    @PrePersist
    @SuppressWarnings("unused")
    void prePersist() {
        if (pickedAt == null) pickedAt = Instant.now();
    }
}
