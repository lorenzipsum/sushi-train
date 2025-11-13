package com.lorenzipsum.sushitrain.backend.domain.belt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;


/**
 * Belt rotation is simulated using 'rotationOffset' containing a value from '0' to 'slotCount-1'
 * UI computes positions as (positionIndex + rotationOffset) % slotCount
 */
@SuppressWarnings("LombokGetterMayBeUsed")
public class Belt {
    private final UUID id;
    private final String name;
    private final int slotCount;
    private final List<BeltSlot> slots = new ArrayList<>();
    private int rotationOffset;
    private int tickIntervalMs;
    private int speedSlotsPerTick;

    private Belt(UUID id, String name, int slotCount, int rotationOffset, int tickIntervalMs, int speedSlotsPerTick) {
        this.id = id;
        this.name = name;
        this.slotCount = Math.max(1, slotCount);
        this.rotationOffset = ((rotationOffset % slotCount) + slotCount) % slotCount; // normalize
        this.tickIntervalMs = Math.max(1, tickIntervalMs);
        this.speedSlotsPerTick = Math.max(1, speedSlotsPerTick);
        IntStream.range(0, slotCount).forEach(i ->
                this.slots.add(BeltSlot.createEmptyAt(id, i)));
    }

    public static Belt create(String name, int slotCount) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (slotCount <= 0) throw new IllegalArgumentException("slotCount must be > 0");
        return new Belt(UUID.randomUUID(), name, slotCount, 0, 1000, 1);
    }

    public static Belt rehydrate(UUID id, String name, int slotCount, int rotationOffset, int tickIntervalMs, int speedSlotsPerTick) {
        return new Belt(id, name, slotCount, rotationOffset, tickIntervalMs, speedSlotsPerTick);
    }

    // public setters containing invariants
    public void advanceOffset() {
        rotationOffset = (rotationOffset + speedSlotsPerTick) % slotCount;
    }

    public void setSpeedSlotsPerTick(int speedSlotsPerTick) {
        if (speedSlotsPerTick >= slotCount)
            throw new IllegalArgumentException("Speed slots per tick cannot has to be smaller than amount of slots");
        this.speedSlotsPerTick = Math.max(1, speedSlotsPerTick);
    }

    public void setTickIntervalMs(int tickIntervalMs) {
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

    public int getRotationOffset() {
        return rotationOffset;
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
