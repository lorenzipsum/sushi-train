package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "belt_slot",
        uniqueConstraints = @UniqueConstraint(name = "uk_belt_position", columnNames = {"belt_id", "position_index"})
)
@Getter
public class BeltSlotEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "belt_id", nullable = false)
    @Setter
    private BeltEntity belt;

    @Column(name = "position_index", nullable = false)
    private int positionIndex;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plate_id")
    @Setter
    private PlateEntity plate;

    @SuppressWarnings("unused")
    protected BeltSlotEntity() {
    }

    public BeltSlotEntity(UUID id, int positionIndex, BeltEntity belt, PlateEntity plate) {
        this.id = id;
        this.positionIndex = positionIndex;
        this.plate = plate;
        this.belt = belt;
    }
}
