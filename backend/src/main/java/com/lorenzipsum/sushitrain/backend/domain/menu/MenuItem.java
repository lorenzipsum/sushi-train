package com.lorenzipsum.sushitrain.backend.domain.menu;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "menu_items")
@Getter
public class MenuItem {
    @Id
    private UUID id;

    private String name;

    @Enumerated(EnumType.STRING)
    private PlateTier defaultTier;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "base_price_yen"))
    private MoneyYen basePrice;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected MenuItem() {
    }

    private MenuItem(UUID id, String name, PlateTier defaultTier, MoneyYen basePrice, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.defaultTier = defaultTier;
        this.basePrice = basePrice;
        this.createdAt = createdAt;
    }

    public static MenuItem create(String name, PlateTier defaultTier, MoneyYen basePrice) {
        return new MenuItem(UUID.randomUUID(), name, defaultTier, basePrice, Instant.now());
    }
}
