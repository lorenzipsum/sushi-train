package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity;

import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * MoneyYen is stored as an INT column (base_price_yen).
 */
@Entity
@Table(name = "menu_item")
@Getter
@Setter
public class MenuItemEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_tier", nullable = false)
    private PlateTier defaultTier;

    @Column(name = "base_price_yen", nullable = false)
    private int basePriceYen;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @SuppressWarnings("unused")
    protected MenuItemEntity() {
    }

    public MenuItemEntity(UUID id, String name, PlateTier defaultTier, int basePriceYen, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.defaultTier = defaultTier;
        this.basePriceYen = basePriceYen;
        this.createdAt = createdAt;
    }

    @PrePersist
    @SuppressWarnings("unused")
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}