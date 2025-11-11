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
@Table(name = "plate")
@Getter
public class Plate {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_snapshot", nullable = false, length = 16)
    private PlateTier tierSnapshot;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "price_at_creation_yen", nullable = false))
    private MoneyYen priceAtCreation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private PlateStatus status;

    //optimistic concurrency
    @Version
    @SuppressWarnings("unused")
    private long version;

    @SuppressWarnings("unused")
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
        if (menuItem == null) throw new IllegalArgumentException("Menu item cannot be null");
        if (effectiveTier == null) throw new IllegalArgumentException("Plate tier cannot be null");
        if (price == null) throw new IllegalArgumentException("Price cannot be null");
        if (expiresAt == null) throw new IllegalArgumentException("Expiration cannot be null");
        if (expiresAt.isBefore(Instant.now())) throw new IllegalArgumentException("Expiration must be in the future");
        return new Plate(UUID.randomUUID(), menuItem, effectiveTier, price, Instant.now(), expiresAt, PlateStatus.ON_BELT);
    }

    public static Plate create(MenuItem menuItem, Instant expiresAt) {
        if (menuItem == null) throw new IllegalArgumentException("Menu item cannot be null");
        return create(menuItem, menuItem.getDefaultTier(), menuItem.getBasePrice(), expiresAt);
    }

    public void expire() {
        if (status != PlateStatus.EXPIRED) this.status = PlateStatus.EXPIRED;
    }

    @PrePersist
    @SuppressWarnings("unused")
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
