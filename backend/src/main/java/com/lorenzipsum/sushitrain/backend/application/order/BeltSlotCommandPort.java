package com.lorenzipsum.sushitrain.backend.application.order;

import java.util.UUID;

public interface BeltSlotCommandPort {
    void clearPlateAssignment(UUID plateId);
}
