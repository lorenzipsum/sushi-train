package com.lorenzipsum.sushitrain.backend.domain.plate;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItem;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "plates")
@Getter
public class Plate {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_snapshot", nullable = false)
    private PlateTier tierSnapshot;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "price_at_creation_yen"))
    private MoneyYen priceAtCreation;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PlateStatus status;

    protected Plate() {
    }

    private Plate(UUID id, MenuItem menuItem, PlateTier tierSnapshot, MoneyYen priceAtCreation, Instant createdAt, Instant expiresAt, PlateStatus status) {
        this.id = id;
        this.menuItem = menuItem;
        this.tierSnapshot = tierSnapshot;
        this.priceAtCreation = priceAtCreation;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    public static Plate create(MenuItem menuItem, PlateTier effectiveTier, MoneyYen price, Instant expiresAt) {
        return new Plate(UUID.randomUUID(), menuItem, effectiveTier, price, Instant.now(), expiresAt, PlateStatus.ON_BELT);
    }

    public void expire() {
        this.status = PlateStatus.EXPIRED;
    }
}
