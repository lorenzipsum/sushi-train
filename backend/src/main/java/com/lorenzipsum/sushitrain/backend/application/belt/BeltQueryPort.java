package com.lorenzipsum.sushitrain.backend.application.belt;

import com.lorenzipsum.sushitrain.backend.application.view.BeltSlotPlateView;
import com.lorenzipsum.sushitrain.backend.application.view.SeatStateView;

import java.util.List;
import java.util.UUID;

public interface BeltQueryPort {
    List<BeltSlotPlateView> findBeltSnapshot(UUID beltId);

    List<SeatStateView> findSeatStates(UUID beltId);
}
