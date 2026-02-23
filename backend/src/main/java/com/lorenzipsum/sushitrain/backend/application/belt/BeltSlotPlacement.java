package com.lorenzipsum.sushitrain.backend.application.belt;

import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltSlotEntity;

import java.util.ArrayList;
import java.util.List;

final class BeltSlotPlacement {
    private BeltSlotPlacement() {}

    /**
     * Picks slots from a list already ordered by positionIndex.
     * Rule: first chosen slot is the first free one; next must be at least minGapSlots ahead.
     * Wrap-around ignored by design.
     */
    static List<BeltSlotEntity> pickSlots(List<BeltSlotEntity> freeSlotsOrdered, int minGapSlots, int count) {
        var picked = new ArrayList<BeltSlotEntity>(count);
        Integer lastPos = null;

        for (var slot : freeSlotsOrdered) {
            if (picked.size() >= count) break;

            int pos = slot.getPositionIndex();
            if (lastPos == null || (pos - lastPos) >= minGapSlots) {
                picked.add(slot);
                lastPos = pos;
            }
        }

        return picked;
    }
}