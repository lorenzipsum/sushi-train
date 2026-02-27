package com.lorenzipsum.sushitrain.backend.infrastructure.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@EnableConfigurationProperties(DataIntegrityRepairJobProperties.class)
@Component
class DataIntegrityRepairScheduler {

    private static final Logger log = LoggerFactory.getLogger(DataIntegrityRepairScheduler.class);

    private final DataIntegrityRepairService service;
    private final DataIntegrityRepairJobProperties props;
    private final AtomicBoolean running = new AtomicBoolean(false);

    DataIntegrityRepairScheduler(DataIntegrityRepairService service, DataIntegrityRepairJobProperties props) {
        this.service = service;
        this.props = props;
    }

    @Scheduled(
            fixedDelayString = "${app.data-integrity-repair.fixed-delay:5m}",
            initialDelayString = "${app.data-integrity-repair.initial-delay:30s}"
    )
    void repairDataIntegrityOnSchedule() {
        if (!props.enabled()) {
            return;
        }

        if (!running.compareAndSet(false, true)) {
            log.debug("Data integrity repair job is still running, skipping this tick.");
            return;
        }

        try {
            var summary = service.repairKnownAnomalies();
            if (summary.detectedPlates() > 0) {
                log.warn(
                        "Repaired data anomalies: detectedPlates={}, clearedSlots={}, markedPicked={}",
                        summary.detectedPlates(),
                        summary.clearedSlots(),
                        summary.markedPicked()
                );
            } else {
                log.debug("No data anomalies detected.");
            }
        } catch (RuntimeException ex) {
            log.error("Data integrity repair job failed.", ex);
            throw ex;
        } finally {
            running.set(false);
        }
    }
}
