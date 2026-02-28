package com.lorenzipsum.sushitrain.backend.infrastructure.scheduling;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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
    private final Counter runsCounter;
    private final Counter detectedPlatesCounter;
    private final Counter repairedSlotsCounter;
    private final Counter repairedPlatesCounter;
    private final Counter repairedOrdersCounter;
    private final Counter failuresCounter;

    DataIntegrityRepairScheduler(DataIntegrityRepairService service, DataIntegrityRepairJobProperties props, MeterRegistry meterRegistry) {
        this.service = service;
        this.props = props;
        this.runsCounter = meterRegistry.counter("sushitrain.data_integrity_repair.runs");
        this.detectedPlatesCounter = meterRegistry.counter("sushitrain.data_integrity_repair.detected_plates");
        this.repairedSlotsCounter = meterRegistry.counter("sushitrain.data_integrity_repair.repaired_slots");
        this.repairedPlatesCounter = meterRegistry.counter("sushitrain.data_integrity_repair.repaired_plates");
        this.repairedOrdersCounter = meterRegistry.counter("sushitrain.data_integrity_repair.repaired_orders");
        this.failuresCounter = meterRegistry.counter("sushitrain.data_integrity_repair.failures");
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
            runsCounter.increment();
            var summary = service.repairKnownAnomalies();
            detectedPlatesCounter.increment(summary.detectedPlates());
            repairedSlotsCounter.increment(summary.clearedSlots());
            repairedPlatesCounter.increment(summary.markedPicked());
            repairedOrdersCounter.increment(summary.duplicateOpenOrdersClosed());
            if (summary.detectedPlates() > 0 || summary.duplicateOpenOrdersClosed() > 0) {
                log.warn(
                        "Repaired data anomalies: detectedPlates={}, clearedSlots={}, markedPicked={}, duplicateOpenOrdersClosed={}",
                        summary.detectedPlates(),
                        summary.clearedSlots(),
                        summary.markedPicked(),
                        summary.duplicateOpenOrdersClosed()
                );
            } else {
                log.debug("No data anomalies detected.");
            }
        } catch (RuntimeException ex) {
            failuresCounter.increment();
            log.error("Data integrity repair job failed.", ex);
            throw ex;
        } finally {
            running.set(false);
        }
    }
}
