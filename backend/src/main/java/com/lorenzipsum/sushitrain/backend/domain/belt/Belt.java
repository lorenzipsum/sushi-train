package com.lorenzipsum.sushitrain.backend.domain.belt;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Entity
@Table(name = "belts")
@Getter
public class Belt {
    @Id
    private UUID id;

    private String name;

    @Column(name = "slot_count", nullable = false)
    private int slotCount;

    /**
     * 0..slotCount-1; UI computes positions as (positionIndex + rotationOffset) % slotCount
     */
    @Column(name = "rotation_offset", nullable = false)
    private int rotationOffset = 0;

    @Setter
    @Column(name = "tick_interval_ms", nullable = false)
    private int tickIntervalMs = 1000;

    @Column(name = "speed_slots_per_tick", nullable = false)
    private int speedSlotsPerTick = 1;

    @OneToMany(mappedBy = "belt", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<BeltSlot> slots = new ArrayList<>();

    @SuppressWarnings("unused")
    protected Belt() {
    }

    private Belt(UUID id, String name, int slotCount) {
        this.id = id;
        this.name = name;
        this.slotCount = Math.max(1, slotCount);
    }

    public static Belt create(String name, int slotCount) {
        var belt = new Belt(UUID.randomUUID(), name, slotCount);
        IntStream.range(0, belt.slotCount).forEach(i -> belt.slots.add(BeltSlot.emptyAt(belt, i)));
        return belt;
    }

    public void advanceOffset() {
        rotationOffset = (rotationOffset + speedSlotsPerTick) % slotCount;
    }

    public void setSpeedSlotsPerTick(int s) {
        this.speedSlotsPerTick = Math.max(1, s);
    }

    public List<BeltSlot> getSlots() {
        return Collections.unmodifiableList(slots);
    }
}
