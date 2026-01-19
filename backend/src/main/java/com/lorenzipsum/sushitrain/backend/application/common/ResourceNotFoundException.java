package com.lorenzipsum.sushitrain.backend.application.common;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, UUID id) {
        super(resource + " not found: " + id);
    }
}
