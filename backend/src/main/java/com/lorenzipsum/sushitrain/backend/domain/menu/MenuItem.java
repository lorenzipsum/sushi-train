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

    @SuppressWarnings("unused")
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
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name must not be blank");
        if (defaultTier == null) throw new IllegalArgumentException("Default tier cannot be null");
        if (basePrice == null) throw new IllegalArgumentException("Base price cannot be null");
        return new MenuItem(UUID.randomUUID(), name.trim(), defaultTier, basePrice, Instant.now());
    }

    @SuppressWarnings("unused")
    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
