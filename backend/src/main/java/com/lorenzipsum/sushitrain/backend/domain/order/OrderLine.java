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
    private UUID uuid;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plate_id")
    private Plate plate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "menu_item_name_snapshot", nullable = false)
    private String menuItemNameSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_snapshot", nullable = false)
    private PlateTier tierSnapshot;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "price_at_pick_yen"))
    private MoneyYen priceAtPickYen;

    @Column(name = "picket_at", nullable = false)
    private Instant pickedAt;

    protected OrderLine() {
    }

    private OrderLine(UUID uuid, Plate plate, Order order, String menuItemNameSnapshot, PlateTier tierSnapshot, MoneyYen priceAtPickYen, Instant pickedAt) {
        this.uuid = uuid;
        this.plate = plate;
        this.order = order;
        this.menuItemNameSnapshot = menuItemNameSnapshot;
        this.tierSnapshot = tierSnapshot;
        this.priceAtPickYen = priceAtPickYen;
        this.pickedAt = pickedAt;
    }

    public static OrderLine create(Plate plate, Order order, int priceYen) {
        return new OrderLine(UUID.randomUUID(), plate, order, plate.getMenuItem().getName(), plate.getTierSnapshot(), new MoneyYen(priceYen), Instant.now());
    }
}
