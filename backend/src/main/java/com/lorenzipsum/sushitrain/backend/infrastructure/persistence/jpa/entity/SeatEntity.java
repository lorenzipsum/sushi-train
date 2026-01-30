package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "seat",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_belt_label", columnNames = {"belt_id", "label"}),
                @UniqueConstraint(name = "uk_seat_position", columnNames = {"belt_id", "position_index"})
        }
)
@Getter
public class SeatEntity {
    @Id
    private UUID id;

    @Column(name = "label", nullable = false, length = 32)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "belt_id", nullable = false)
    @Setter
    private BeltEntity belt;

    @Column(name = "position_index", nullable = false)
    private int positionIndex;

    @SuppressWarnings("unused")
    protected SeatEntity() {
    }

    public SeatEntity(UUID id, String label, BeltEntity belt, int positionIndex) {
        this.id = id;
        this.label = label;
        this.belt = belt;
        this.positionIndex = positionIndex;
    }
}
