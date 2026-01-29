package com.lorenzipsum.sushitrain.backend.domain.seat;

import java.util.Locale;
import java.util.UUID;

@SuppressWarnings({"LombokGetterMayBeUsed", "ClassCanBeRecord"})
public class Seat {
    private final UUID id;
    private final String label;
    private final UUID beltId;
    private final int positionIndex;

    private Seat(UUID id, String label, UUID beltId, int positionIndex) {
        this.id = id;
        this.label = label;
        this.beltId = beltId;
        this.positionIndex = positionIndex;
    }

    public static Seat create(String label, UUID beltId, int seatPositionIndex) {
        if (beltId == null) throw new IllegalArgumentException("Belt cannot be null");
        if (seatPositionIndex < 0) throw new IllegalArgumentException("Seat position cannot be negative");
        String cleanLabel = normalizeLabel(label);
        return new Seat(UUID.randomUUID(), cleanLabel, beltId, seatPositionIndex);
    }

    public static Seat rehydrate(UUID id, String label, UUID beltId, int seatPositionIndex) {
        return new Seat(id, label, beltId, seatPositionIndex);
    }

    private static String normalizeLabel(String raw) {
        if (raw == null) throw new IllegalArgumentException("Label cannot be null");
        String s = raw.trim().toUpperCase(Locale.ROOT);
        if (s.isEmpty()) throw new IllegalArgumentException("Label cannot be null or empty");
        if (!s.matches("[A-Z0-9]+"))
            throw new IllegalArgumentException("Label must be alphanumeric (A–Z, 0–9), got: '" + s + "'");
        return s;
    }

    public UUID getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public UUID getBeltId() {
        return beltId;
    }

    public int getPositionIndex() {
        return positionIndex;
    }
}
