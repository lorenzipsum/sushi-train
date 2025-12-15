package com.lorenzipsum.sushitrain.backend.domain.belt;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@SuppressWarnings("LombokGetterMayBeUsed")
public class BeltSlot {
    private final UUID id;
    private final UUID beltId;
    private final int positionIndex;
    private UUID plateId;

    public BeltSlot(UUID id, UUID beltId, int positionIndex, UUID plateId) {
        this.id = id;
        this.beltId = beltId;
        this.positionIndex = positionIndex;
        this.plateId = plateId;
    }

    public static BeltSlot createEmptyAt(UUID beltId, int positionIndex) {
        if (beltId == null) throw new IllegalArgumentException("Belt Id cannot be null");
        if (positionIndex < 0) throw new IllegalArgumentException("Position index cannot be negative");
        UUID slotId = UUID.nameUUIDFromBytes((beltId + ":" + positionIndex).getBytes(StandardCharsets.UTF_8));
        return new BeltSlot(slotId, beltId, positionIndex, null);
    }

    public boolean isEmpty() {
        return plateId == null;
    }

    public void place(UUID plateId) {
        if (plateId == null) throw new IllegalArgumentException("plateId must not be null");
        if (this.plateId != null) throw new IllegalStateException("slot already occupied");
        this.plateId = plateId;
    }

    public UUID take() {
        UUID p = this.plateId;
        this.plateId = null;
        return p;
    }

    public UUID takeStrict() {
        if (this.plateId == null) throw new IllegalStateException("slot is empty");
        return take();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getBeltId() {
        return beltId;
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    public UUID getPlateId() {
        return plateId;
    }
}
