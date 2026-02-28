package com.lorenzipsum.sushitrain.backend.application.plate;

import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PlateExpiryService {

    private final PlateExpiryCommandPort plateExpiryCommandPort;

    public PlateExpiryService(PlateExpiryCommandPort plateExpiryCommandPort) {
        this.plateExpiryCommandPort = plateExpiryCommandPort;
    }

    /**
     * Idempotent:
     * - if a plate is already EXPIRED, nothing happens
     * - if a plate is not on a belt slot anymore, belt slot update affects 0 rows and that's fine
     */
    @Transactional
    public int expirePlatesNow() {
        Instant now = Instant.now();

        List<UUID> expiredPlateIds = plateExpiryCommandPort.findExpiredPlateIds(
                List.of(PlateStatus.CREATED, PlateStatus.ON_BELT),
                now
        );

        if (expiredPlateIds.isEmpty()) {
            return 0;
        }

        // TODO (batching): If the system grows (many belts / many plates), implement process in chunks (e.g. 500 ids at a time)

        // 1) mark plates as EXPIRED (only if they are still CREATED/ON_BELT, keeps it idempotent)
        int updatedPlates = plateExpiryCommandPort.markExpired(
                expiredPlateIds,
                List.of(PlateStatus.CREATED, PlateStatus.ON_BELT)
        );

        // 2) remove plates from belt slots (if assigned)
        plateExpiryCommandPort.clearPlateAssignments(expiredPlateIds);

        return updatedPlates;
    }
}
