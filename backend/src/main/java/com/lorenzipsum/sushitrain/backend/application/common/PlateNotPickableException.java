package com.lorenzipsum.sushitrain.backend.application.common;

import java.util.UUID;

public class PlateNotPickableException extends RuntimeException {
    public PlateNotPickableException(UUID plateId) {
        super("Plate is not pickable: " + plateId);
    }
}
