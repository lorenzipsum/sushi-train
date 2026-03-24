package com.lorenzipsum.sushitrain.backend.infrastructure.version;

public record VersionInfo(
        String service,
        String version,
        String commitSha,
        String buildTime,
        String environment
) {
}
