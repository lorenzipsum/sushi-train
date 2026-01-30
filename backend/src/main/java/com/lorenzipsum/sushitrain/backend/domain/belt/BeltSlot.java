package com.lorenzipsum.sushitrain.backend.domain.belt;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@SuppressWarnings("LombokGetterMayBeUsed")
public class BeltSlot {
    private final UUID id;
    private final int positionIndex;
    private UUID plateId;

    public BeltSlot(UUID id, int positionIndex, UUID plateId) {
        this.id = id;
        this.positionIndex = positionIndex;
        this.plateId = plateId;
    }

    static BeltSlot createEmptyAt(UUID beltId, int positionIndex) {
        if (beltId == null) throw new IllegalArgumentException("beltId must not be null");
        if (positionIndex < 0) throw new IllegalArgumentException("Position index cannot be negative");
        UUID slotId = UUID.nameUUIDFromBytes((beltId + ":SLOT:" + positionIndex).getBytes(StandardCharsets.UTF_8));
        return new BeltSlot(slotId, positionIndex, null);
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

    public UUID getId() {
        return id;
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    public UUID getPlateId() {
        return plateId;
    }
}
