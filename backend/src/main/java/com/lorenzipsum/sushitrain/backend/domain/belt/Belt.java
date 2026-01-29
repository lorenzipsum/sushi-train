package com.lorenzipsum.sushitrain.backend.domain.belt;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@SuppressWarnings("LombokGetterMayBeUsed")
public class Belt {
    private static final int DEFAULT_TICK_INTERVAL_MS = 1000;
    private static final int DEFAULT_BASE_ROTATION_OFFSET = 0;
    private static final int DEFAULT_SPEED_SLOTS_PER_TICK = 1;
    private final UUID id;
    private final String name;
    private final int slotCount;
    private final List<BeltSlot> slots;
    private int baseRotationOffset;
    private Instant offsetStartedAt;
    private int tickIntervalMs;
    private int speedSlotsPerTick;

    private Belt(UUID id, String name, int slotCount, int baseRotationOffset, int tickIntervalMs, int speedSlotsPerTick, List<BeltSlot> slots, Instant offsetStartedAt) {
        this.id = id;
        this.name = name;
        this.slotCount = Math.max(1, slotCount);
        this.offsetStartedAt = offsetStartedAt == null ? Instant.EPOCH : offsetStartedAt;
        this.baseRotationOffset = normalizeOffset(baseRotationOffset, slotCount);
        this.tickIntervalMs = Math.max(1, tickIntervalMs);
        this.speedSlotsPerTick = Math.max(1, speedSlotsPerTick);
        this.slots = (slots == null) ? new ArrayList<>() : new ArrayList<>(slots);
    }

    public static Belt create(String name, int slotCount) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (slotCount <= 0) throw new IllegalArgumentException("slotCount must be > 0");
        UUID id = UUID.randomUUID();
        List<BeltSlot> slots = IntStream.range(0, slotCount)
                .mapToObj(i -> BeltSlot.createEmptyAt(id, i))
                .toList();
        return new Belt(id, name, slotCount, DEFAULT_BASE_ROTATION_OFFSET, DEFAULT_TICK_INTERVAL_MS, DEFAULT_SPEED_SLOTS_PER_TICK, slots, Instant.now());
    }

    public static Belt rehydrate(UUID id, String name, int slotCount, int baseRotationOffset, int tickIntervalMs, int speedSlotsPerTick, List<BeltSlot> slots, Instant offsetStartedAt) {
        return new Belt(id, name, slotCount, baseRotationOffset, tickIntervalMs, speedSlotsPerTick, slots, offsetStartedAt);
    }

    private static int normalizeOffset(int offset, int slotCount) {
        return ((offset % slotCount) + slotCount) % slotCount;
    }

    public int currentOffsetAt(Instant now) {
        if (now == null) throw new IllegalArgumentException("Timestamp 'now' cannot be null.");
        long elapsedMs = Duration.between(offsetStartedAt, now).toMillis();
        if (elapsedMs < 0) elapsedMs = 0;

        long elapsedTicks = elapsedMs / tickIntervalMs;
        long offset = baseRotationOffset + elapsedTicks * (long) speedSlotsPerTick % slotCount;
        return (int) offset;
    }

    public void rebaseOffsetAt(Instant now) {
        this.baseRotationOffset = currentOffsetAt(now);
        this.offsetStartedAt = now;
    }

    // public setters containing invariants
    public void setSpeedSlotsPerTick(int speedSlotsPerTick, Instant now) {
        if (speedSlotsPerTick >= slotCount)
            throw new IllegalArgumentException("Speed slots per tick cannot has to be smaller than amount of slots");
        if (now == null) throw new IllegalArgumentException("Timestamp 'now' cannot be null");

        rebaseOffsetAt(now);
        this.speedSlotsPerTick = Math.max(1, speedSlotsPerTick);
    }

    public void setTickIntervalMs(int tickIntervalMs, Instant now) {
        if (now == null) throw new IllegalArgumentException("Timestamp 'now' cannot be null");

        rebaseOffsetAt(now);
        this.tickIntervalMs = Math.max(1, tickIntervalMs);
    }

    // public getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getSlotCount() {
        return slotCount;
    }

    public int getBaseRotationOffset() {
        return baseRotationOffset;
    }

    public Instant getOffsetStartedAt() {
        return offsetStartedAt;
    }

    public int getTickIntervalMs() {
        return tickIntervalMs;
    }

    public int getSpeedSlotsPerTick() {
        return speedSlotsPerTick;
    }

    public List<BeltSlot> getSlots() {
        return Collections.unmodifiableList(slots);
    }
}
