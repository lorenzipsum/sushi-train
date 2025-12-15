package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "belt")
@Getter
public class BeltEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slot_count", nullable = false)
    private int slotCount;

    @Column(name = "rotation_offset", nullable = false)
    @Setter
    private int rotationOffset;

    @Column(name = "tick_interval_ms", nullable = false)
    @Setter
    private int tickIntervalMs;

    @Column(name = "speed_slots_per_tick", nullable = false)
    @Setter
    private int speedSlotsPerTick;

    @OneToMany(mappedBy = "belt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("positionIndex ASC")
    private final List<BeltSlotEntity> slots = new ArrayList<>();

    @SuppressWarnings("unused")
    protected BeltEntity() {
    }

    public BeltEntity(UUID id, String name, int slotCount, int rotationOffset, int tickIntervalMs, int speedSlotsPerTick) {
        this.id = id;
        this.name = name;
        this.slotCount = slotCount;
        this.rotationOffset = rotationOffset;
        this.tickIntervalMs = tickIntervalMs;
        this.speedSlotsPerTick = speedSlotsPerTick;
    }

    @SuppressWarnings("unused")
    public void replaceSlots(List<BeltSlotEntity> newSlots) {
        this.slots.clear();
        this.slots.addAll(newSlots);
        // set back-reference
        for (BeltSlotEntity s : this.slots) {
            s.setBelt(this);
        }
    }
}
