package com.lorenzipsum.sushitrain.backend.domain.menu;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;


@SuppressWarnings("LombokGetterMayBeUsed")
public class MenuItem {
    private UUID id;
    private String name;
    private PlateTier defaultTier;
    private MoneyYen basePrice;
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

    /**
     * Rehydration factory for adapters (persistence).
     */
    public static MenuItem rehydrate(UUID id, String name, PlateTier defaultTier, MoneyYen basePrice, Instant createdAt) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(defaultTier, "defaultTier");
        Objects.requireNonNull(basePrice, "basePrice");
        Objects.requireNonNull(createdAt, "createdAt");
        return new MenuItem(id, name, defaultTier, basePrice, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PlateTier getDefaultTier() {
        return defaultTier;
    }

    public MoneyYen getBasePrice() {
        return basePrice;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
