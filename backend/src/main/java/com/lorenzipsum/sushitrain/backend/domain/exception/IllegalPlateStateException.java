package com.lorenzipsum.sushitrain.backend.domain.exception;

import java.util.UUID;

public class IllegalPlateStateException extends IllegalStateException {
    public IllegalPlateStateException(String message, UUID plateId) {
        super(message + " (plateId:'" + plateId + "')");
    }
}
