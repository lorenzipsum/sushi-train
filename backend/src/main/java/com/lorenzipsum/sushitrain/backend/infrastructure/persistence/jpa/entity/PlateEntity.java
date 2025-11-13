package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "plate")
@Getter
public class PlateEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItemEntity menuItem;

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
    protected PlateEntity() {
    }

    public PlateEntity(UUID id, MenuItemEntity menuItemEntity, PlateTier tierSnapshot, MoneyYen priceAtCreation, Instant createdAt, Instant expiresAt, PlateStatus status) {
        this.id = id;
        this.menuItem = menuItemEntity;
        this.tierSnapshot = tierSnapshot;
        this.priceAtCreation = priceAtCreation;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    @PrePersist
    @SuppressWarnings("unused")
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
