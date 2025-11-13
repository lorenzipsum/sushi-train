package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "seat", uniqueConstraints = @UniqueConstraint(name = "uk_belt_label", columnNames = {"belt_id", "label"}))
@Getter
public class SeatEntity {
    @Id
    private UUID id;

    @Column(name = "label", nullable = false, length = 32)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "belt_id", nullable = false)
    private BeltEntity belt;

    @Column(nullable = false)
    private int seatPositionIndex;

    @SuppressWarnings("unused")
    protected SeatEntity() {
    }

    public SeatEntity(UUID id, String label, BeltEntity belt, int seatPositionIndex) {
        this.id = id;
        this.label = label;
        this.belt = belt;
        this.seatPositionIndex = seatPositionIndex;
    }
}
