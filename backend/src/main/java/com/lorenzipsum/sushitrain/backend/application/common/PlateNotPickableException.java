package com.lorenzipsum.sushitrain.backend.application.common;

import java.util.UUID;

public class PlateNotPickableException extends RuntimeException {
    private final UUID plateId;

    public PlateNotPickableException(UUID plateId) {
        super("Plate is not pickable: " + plateId);
        this.plateId = plateId;
    }

    public UUID plateId() {
        return plateId;
    }
}
