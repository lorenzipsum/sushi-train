package com.lorenzipsum.sushitrain.backend.infrastructure.version;

import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class VersionInfoService {
    private final Environment environment;
    private final BuildProperties buildProperties;
    private final GitProperties gitProperties;

    public VersionInfoService(
            Environment environment,
            @Nullable BuildProperties buildProperties,
            @Nullable GitProperties gitProperties
    ) {
        this.environment = environment;
        this.buildProperties = buildProperties;
        this.gitProperties = gitProperties;
    }

    public VersionInfo getVersionInfo() {
        return new VersionInfo(
                environment.getProperty("spring.application.name"),
                getVersion(),
                getCommitSha(),
                getBuildTime(),
                normalize(environment.getProperty("app.environment"))
        );
    }

    private String getVersion() {
        if (buildProperties != null) {
            String version = normalize(buildProperties.getVersion());
            if (version != null) {
                return version;
            }
        }
        String configuredVersion = normalize(environment.getProperty("app.version"));
        if (configuredVersion != null) {
            return configuredVersion;
        }
        return normalize(VersionInfoService.class.getPackage().getImplementationVersion());
    }

    private String getCommitSha() {
        if (gitProperties != null) {
            String commitId = normalize(gitProperties.get("commit.id.abbrev"));
            if (commitId != null) {
                return commitId;
            }
            return normalize(gitProperties.getCommitId());
        }
        return normalize(environment.getProperty("app.commit-sha"));
    }

    private String getBuildTime() {
        if (buildProperties != null && buildProperties.getTime() != null) {
            return buildProperties.getTime().toString();
        }
        return normalize(environment.getProperty("app.build-time"));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
