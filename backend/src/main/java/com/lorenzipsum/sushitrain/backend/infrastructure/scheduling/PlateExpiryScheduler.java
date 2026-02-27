package com.lorenzipsum.sushitrain.backend.infrastructure.scheduling;

import com.lorenzipsum.sushitrain.backend.application.plate.PlateExpiryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@EnableScheduling
@EnableConfigurationProperties(PlateExpiryJobProperties.class)
@Component
class PlateExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(PlateExpiryScheduler.class);

    private final PlateExpiryService service;
    private final PlateExpiryJobProperties props;

    /**
     * Guard to ensure: "no overlapping runs" in this JVM.
     * This is especially helpful while debugging (breakpoints etc.).
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    PlateExpiryScheduler(PlateExpiryService service, PlateExpiryJobProperties props) {
        this.service = service;
        this.props = props;
    }

    @Scheduled(
            fixedDelayString = "${app.plate-expiry.fixed-delay:60s}",
            initialDelayString = "${app.plate-expiry.initial-delay:30s}"
    )
    void expirePlatesOnSchedule() {
        if (!props.enabled()) {
            return;
        }

        // No parallel execution in the same JVM:
        if (!running.compareAndSet(false, true)) {
            log.debug("Plate expiry job is still running, skipping this tick.");
            return;
        }

        try {
            int expiredCount = service.expirePlatesNow();
            if (expiredCount > 0) {
                log.info("Expired {} plate(s).", expiredCount);
            } else {
                log.debug("No expired plates found.");
            }
        } catch (RuntimeException ex) {
            log.error("Plate expiry job failed.", ex);
            throw ex;
        } finally {
            running.set(false);
        }

        // TODO (locking): If this runs in multiple app instances (or multiple scheduler threads),
        //  a distributed lock (e.g., ShedLock or a DB-based lock) should be added so only ONE instance expires plates at a time
    }
}