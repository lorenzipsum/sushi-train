package com.lorenzipsum.sushitrain.backend.domain.seat;

import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "seat", uniqueConstraints = @UniqueConstraint(name = "uk_belt_label", columnNames = {"belt_id", "label"}))
@Getter
public class Seat {
    @Id
    private UUID id;

    @Column(name = "label", nullable = false, length = 32)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "belt_id", nullable = false)
    private Belt belt;

    @Column(nullable = false)
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
        if (belt == null) throw new IllegalArgumentException("Belt cannot be null");
        if (seatPositionIndex < 0 || seatPositionIndex >= belt.getSlotCount()) {
            throw new IllegalArgumentException(
                    "Seat position index out of range: " + seatPositionIndex +
                            " (valid: 0.." + (belt.getSlotCount() - 1) + ")"
            );
        }
        String cleanLabel = normalizeLabel(label);
        return new Seat(UUID.randomUUID(), cleanLabel, belt, seatPositionIndex);
    }

    private static String normalizeLabel(String raw) {
        if (raw == null) throw new IllegalArgumentException("Label cannot be null");
        String s = raw.trim().toUpperCase(Locale.ROOT);
        if (s.isEmpty()) throw new IllegalArgumentException("Label cannot be null or empty");
        if (!s.matches("[A-Z0-9]+"))
            throw new IllegalArgumentException("Label must be alphanumeric (A–Z, 0–9), got: '" + s + "'");
        return s;
    }
}
