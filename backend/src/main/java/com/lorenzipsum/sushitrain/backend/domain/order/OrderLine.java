package com.lorenzipsum.sushitrain.backend.domain.order;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_lines")
@Getter
public class OrderLine {
    @Id
    private UUID id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plate_id", nullable = false, unique = true)
    private Plate plate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

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
    protected OrderLine() {
    }

    private OrderLine(UUID id, Plate plate, Order order, String menuItemNameSnapshot, PlateTier tierSnapshot, MoneyYen priceAtPick, Instant pickedAt) {
        this.id = id;
        this.plate = plate;
        this.order = order;
        this.menuItemNameSnapshot = menuItemNameSnapshot;
        this.tierSnapshot = tierSnapshot;
        this.priceAtPick = priceAtPick;
        this.pickedAt = pickedAt;
    }

    public static OrderLine create(Plate plate, Order order, int priceInYen) {
        if (plate == null) throw new IllegalArgumentException("Plate cannot be null");
        if (order == null) throw new IllegalArgumentException("Order cannot be null");
        if (priceInYen < 0) throw new IllegalArgumentException("Price cannot be a negative value");
        return new OrderLine(UUID.randomUUID(), plate, order, plate.getMenuItem().getName(), plate.getTierSnapshot(), new MoneyYen(priceInYen), Instant.now());
    }

    @PrePersist
    @SuppressWarnings("unused")
    void prePersist() {
        if (pickedAt == null) pickedAt = Instant.now();
    }

    void clearOrder() {
        this.order = null;
    }
}
