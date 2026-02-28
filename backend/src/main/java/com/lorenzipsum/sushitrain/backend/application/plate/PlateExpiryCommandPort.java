package com.lorenzipsum.sushitrain.backend.application.plate;

import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PlateExpiryCommandPort {
    List<UUID> findExpiredPlateIds(List<PlateStatus> statuses, Instant now);

    int markExpired(List<UUID> plateIds, List<PlateStatus> allowedCurrentStatuses);

    void clearPlateAssignments(List<UUID> plateIds);
}
