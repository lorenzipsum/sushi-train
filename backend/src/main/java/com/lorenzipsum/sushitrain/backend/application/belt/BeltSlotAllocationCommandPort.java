package com.lorenzipsum.sushitrain.backend.application.belt;

import java.util.List;
import java.util.UUID;

public interface BeltSlotAllocationCommandPort {
    List<FreeBeltSlot> findFreeSlotsForUpdate(UUID beltId);

    void assignPlateToSlot(UUID slotId, UUID plateId);

    record FreeBeltSlot(
            UUID slotId,
            int positionIndex
    ) {
    }
}
