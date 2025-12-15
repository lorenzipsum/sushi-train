package com.lorenzipsum.sushitrain.backend.domain.order;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;

import java.time.Instant;
import java.util.UUID;

@SuppressWarnings({"LombokGetterMayBeUsed"})
public class OrderLine {
    private final UUID id;
    private final UUID plateId;
    private UUID orderId;
    private final String menuItemNameSnapshot;
    private final PlateTier tierSnapshot;
    private final MoneyYen priceAtPick;
    private final Instant pickedAt;

    private OrderLine(UUID id, UUID plateId, UUID orderId, String menuItemNameSnapshot, PlateTier tierSnapshot, MoneyYen priceAtPick, Instant pickedAt) {
        this.id = id;
        this.plateId = plateId;
        this.orderId = orderId;
        this.menuItemNameSnapshot = menuItemNameSnapshot;
        this.tierSnapshot = tierSnapshot;
        this.priceAtPick = priceAtPick;
        this.pickedAt = pickedAt;
    }

    public static OrderLine create(UUID plateId, UUID orderId, String menuItemNameSnapshot, PlateTier tierSnapshot, int priceInYen) {
        if (plateId == null) throw new IllegalArgumentException("Plate cannot be null");
        if (orderId == null) throw new IllegalArgumentException("Order cannot be null");
        if (priceInYen < 0) throw new IllegalArgumentException("Price cannot be a negative value");
        return new OrderLine(UUID.randomUUID(), plateId, orderId, menuItemNameSnapshot, tierSnapshot, new MoneyYen(priceInYen), Instant.now());
    }

    public static OrderLine rehydrate(UUID id, UUID plateId, UUID orderId, String menuItemNameSnapshot, PlateTier tierSnapshot, MoneyYen priceAtPick, Instant pickedAt) {
        return new OrderLine(id, plateId, orderId, menuItemNameSnapshot, tierSnapshot, priceAtPick, pickedAt);
    }

    void clearOrder() {
        this.orderId = null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPlateId() {
        return plateId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getMenuItemNameSnapshot() {
        return menuItemNameSnapshot;
    }

    public PlateTier getTierSnapshot() {
        return tierSnapshot;
    }

    public MoneyYen getPriceAtPick() {
        return priceAtPick;
    }

    public Instant getPickedAt() {
        return pickedAt;
    }
}
