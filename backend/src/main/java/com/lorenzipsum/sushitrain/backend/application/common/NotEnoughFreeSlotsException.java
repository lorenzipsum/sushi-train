package com.lorenzipsum.sushitrain.backend.application.common;

import java.util.UUID;

public class NotEnoughFreeSlotsException extends RuntimeException {
    public NotEnoughFreeSlotsException(UUID beltId, int requested, int available) {
        super("Not enough free belt slots for belt " + beltId + ": requested=" + requested + ", available=" + available);
    }
}