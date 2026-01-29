package com.lorenzipsum.sushitrain.backend.domain.plate;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.exception.IllegalPlateStateException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus.EXPIRED;
import static com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus.PICKED;

@SuppressWarnings("LombokGetterMayBeUsed")
public class Plate {
    private final UUID id;
    private final UUID menuItemId;
    private final PlateTier tierSnapshot;
    private final MoneyYen priceAtCreation;
    private final Instant createdAt;
    private final Instant expiresAt;
    private PlateStatus status;

    private Plate(UUID id, UUID menuItemId, PlateTier tierSnapshot, MoneyYen priceAtCreation, Instant createdAt, Instant expiresAt, PlateStatus status) {
        this.id = id;
        this.menuItemId = menuItemId;
        this.tierSnapshot = tierSnapshot;
        this.priceAtCreation = priceAtCreation;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    public static Plate create(UUID menuItemId, PlateTier effectiveTier, MoneyYen price, Instant expiresAt) {
        if (menuItemId == null) throw new IllegalArgumentException("Menu item cannot be null");
        if (effectiveTier == null) throw new IllegalArgumentException("Plate tier cannot be null");
        if (price == null) throw new IllegalArgumentException("Price cannot be null");
        if (expiresAt == null) throw new IllegalArgumentException("Expiration cannot be null");
        if (expiresAt.isBefore(Instant.now())) throw new IllegalArgumentException("Expiration must be in the future");
        return new Plate(UUID.randomUUID(), menuItemId, effectiveTier, price, Instant.now(), expiresAt, PlateStatus.CREATED);
    }

    public static Plate rehydrate(UUID id, UUID menuItemId, PlateTier tierSnapshot, MoneyYen priceAtCreation, Instant createdAt, Instant expiresAt, PlateStatus status) {
        return new Plate(id, menuItemId, tierSnapshot, priceAtCreation, createdAt, expiresAt, status);
    }

    public void expire() {
        if (Objects.requireNonNull(status) == PICKED) {
            throw new IllegalPlateStateException("Cannot expire an already picked plate", id);
        }
        this.status = EXPIRED;
    }

    public void pick() {
        if (Objects.requireNonNull(status) == EXPIRED) {
            throw new IllegalPlateStateException("Cannot pick an expired plate", id);
        }
        this.status = PICKED;
    }

    public void place() {
        switch (status) {
            case EXPIRED -> throw new IllegalPlateStateException("Cannot place an expired plate on the belt", id);
            case PICKED -> throw new IllegalPlateStateException("Cannot place a picked plate on the belt", id);
        }
        this.status = PlateStatus.ON_BELT;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMenuItemId() {
        return menuItemId;
    }

    public PlateTier getTierSnapshot() {
        return tierSnapshot;
    }

    public MoneyYen getPriceAtCreation() {
        return priceAtCreation;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public PlateStatus getStatus() {
        return status;
    }
}
