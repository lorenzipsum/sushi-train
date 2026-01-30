package com.lorenzipsum.sushitrain.backend.domain.belt;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

@SuppressWarnings({"LombokGetterMayBeUsed", "ClassCanBeRecord"})
public class Seat {
    private final UUID id;
    private final String label;
    private final int positionIndex;

    private Seat(UUID id, String label, int positionIndex) {
        this.id = id;
        this.label = label;
        this.positionIndex = positionIndex;
    }

    static Seat createAt(UUID beltId, String label, int positionIndex) {
        if (beltId == null) throw new IllegalArgumentException("beltId must not be null");
        if (positionIndex < 0) throw new IllegalArgumentException("Seat position cannot be negative");
        String cleanLabel = normalizeLabel(label);

        UUID seatId = UUID.nameUUIDFromBytes((beltId + ":SEAT:" + positionIndex).getBytes(StandardCharsets.UTF_8));
        return new Seat(seatId, cleanLabel, positionIndex);
    }

    public static Seat rehydrate(UUID id, String label, int positionIndex) {
        if (id == null) throw new IllegalArgumentException("id must not be null");
        return new Seat(id, label, positionIndex);
    }

    private static String normalizeLabel(String raw) {
        if (raw == null) throw new IllegalArgumentException("Label cannot be null");
        String s = raw.trim().toUpperCase(Locale.ROOT);
        if (s.isEmpty()) throw new IllegalArgumentException("Label cannot be null or empty");
        if (!s.matches("[A-Z0-9]+")) {
            throw new IllegalArgumentException("Label must be alphanumeric (A–Z, 0–9), got: '" + s + "'");
        }
        return s;
    }

    public UUID getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public int getPositionIndex() {
        return positionIndex;
    }
}
