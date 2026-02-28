package com.lorenzipsum.sushitrain.backend.application.plate;

import java.util.UUID;

public interface PlateBeltCommandPort {
    void clearPlateAssignment(UUID plateId);
}
