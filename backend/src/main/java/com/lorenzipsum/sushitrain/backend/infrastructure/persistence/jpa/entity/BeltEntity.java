package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
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

    @Column(name = "base_rotation_offset", nullable = false)
    @Setter
    private int baseRotationOffset;

    @Column(name = "offset_started_at", nullable = false)
    @Setter
    private Instant offsetStartedAt;

    @Column(name = "tick_interval_ms", nullable = false)
    @Setter
    private int tickIntervalMs;

    @Column(name = "speed_slots_per_tick", nullable = false)
    @Setter
    private int speedSlotsPerTick;

    @OneToMany(mappedBy = "belt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("positionIndex ASC")
    private final List<BeltSlotEntity> slots = new ArrayList<>();

    @OneToMany(mappedBy = "belt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("positionIndex ASC")
    private final List<SeatEntity> seats = new ArrayList<>();

    @SuppressWarnings("unused")
    protected BeltEntity() {
    }

    public BeltEntity(UUID id, String name, int slotCount, int baseRotationOffset, Instant offsetStartedAt, int tickIntervalMs, int speedSlotsPerTick) {
        this.id = id;
        this.name = name;
        this.slotCount = slotCount;
        this.baseRotationOffset = baseRotationOffset;
        this.offsetStartedAt = offsetStartedAt;
        this.tickIntervalMs = tickIntervalMs;
        this.speedSlotsPerTick = speedSlotsPerTick;
    }

    public void replaceSlots(List<BeltSlotEntity> newSlots) {
        this.slots.clear();
        this.slots.addAll(newSlots);
        for (BeltSlotEntity s : this.slots) {
            s.setBelt(this);
        }
    }

    public void replaceSeats(List<SeatEntity> newSeats) {
        this.seats.clear();
        this.seats.addAll(newSeats);
        for (SeatEntity s : this.seats) {
            s.setBelt(this);
        }
    }
}
