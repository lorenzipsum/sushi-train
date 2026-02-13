package com.lorenzipsum.sushitrain.backend.application.common;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, UUID id) {
        super(resourceName + " not found: " + id);
    }
}
