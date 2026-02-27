package com.lorenzipsum.sushitrain.backend.application.common;

import java.util.UUID;

public class NotEnoughFreeSlotsException extends RuntimeException {
    private final UUID beltId;
    private final int requested;
    private final int available;

    public NotEnoughFreeSlotsException(UUID beltId, int requested, int available) {
        super("Not enough free belt slots for belt " + beltId + ": requested=" + requested + ", available=" + available);
        this.beltId = beltId;
        this.requested = requested;
        this.available = available;
    }

    public UUID beltId() {
        return beltId;
    }

    public int requested() {
        return requested;
    }

    public int available() {
        return available;
    }
}
