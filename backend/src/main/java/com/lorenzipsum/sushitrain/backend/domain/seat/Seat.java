package com.lorenzipsum.sushitrain.backend.domain.seat;

import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "seats")
@Getter
public class Seat {
    @Id
    private UUID id;

    private String label;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "belt_id")
    private Belt belt;

    private int seatPositionIndex;

    @SuppressWarnings("unused")
    protected Seat() {
    }

    private Seat(UUID id, String label, Belt belt, int seatPositionIndex) {
        this.id = id;
        this.label = label;
        this.belt = belt;
        this.seatPositionIndex = seatPositionIndex;
    }

    public static Seat create(String label, Belt belt, int seatPositionIndex) {
        return new Seat(UUID.randomUUID(), label, belt, seatPositionIndex);
    }
}
