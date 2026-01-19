package com.lorenzipsum.sushitrain.backend.application.plate;

import java.util.UUID;

public class PlateNotFoundException extends RuntimeException {
    public PlateNotFoundException(UUID id) {
        super("Plate not found: " + id);
    }
}
