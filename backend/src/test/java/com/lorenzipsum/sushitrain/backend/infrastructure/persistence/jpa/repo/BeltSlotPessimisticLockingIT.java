package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.hikari.maximum-pool-size=5",
        "spring.datasource.hikari.minimum-idle=2"
})
@SuppressWarnings({"unused", "SameParameterValue", "ResultOfMethodCallIgnored"})
class BeltSlotPessimisticLockingIT {

    @ServiceConnection
    static final PostgreSQLContainer db = new PostgreSQLContainer("postgres:18-alpine");

    @Autowired
    EntityManager em;
    @Autowired
    PlatformTransactionManager txManager;

    @Test
    @DisplayName("PESSIMISTIC: Tx1 locks rows; Tx2 using NOWAIT fails immediately")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @SuppressWarnings({"resource", "SameParameterValue", "ResultOfMethodCallIgnored"})
    void pessimistic_locking_blocks_concurrent_attempt_nowait() throws Exception {
        UUID beltId = UUID.randomUUID();
        seedBeltAndSlotsCommitted(beltId, 20);

        TransactionTemplate tx1 = new TransactionTemplate(txManager);
        TransactionTemplate tx2 = new TransactionTemplate(txManager);

        CountDownLatch tx1HasLock = new CountDownLatch(1);
        CountDownLatch releaseTx1 = new CountDownLatch(1);

        ExecutorService pool = Executors.newFixedThreadPool(2);

        try {
            Future<Void> f1 = pool.submit(() -> {
                tx1.execute(status -> {
                    int locked = lockOneFreeSlotWithoutNowait(beltId);
                    assertThat(locked).as("Tx1 should lock at least one slot row").isEqualTo(1);

                    tx1HasLock.countDown();
                    await(releaseTx1, 5, TimeUnit.SECONDS);
                    return null;
                });
                return null;
            });

            assertThat(tx1HasLock.await(2, TimeUnit.SECONDS))
                    .as("Tx1 should acquire lock before Tx2 starts")
                    .isTrue();

            Future<Void> f2 = pool.submit(() -> {
                assertThatThrownBy(() -> tx2.execute(status -> {
                    lockOneFreeSlotWithNowait(beltId);
                    return null;
                }))
                        .as("Tx2 should fail immediately due to NOWAIT lock conflict")
                        .satisfies(ex -> assertThat(
                                hasPgLockNotAvailable(ex)
                                        || hasCauseNamed(ex, "PessimisticLockException")
                                        || hasCauseNamed(ex, "LockTimeoutException")
                        ).as("Expected a lock-not-available / pessimistic exception, got: " + ex.getClass().getName())
                                .isTrue());

                return null;
            });

            f2.get(10, TimeUnit.SECONDS);

            releaseTx1.countDown();
            f1.get(10, TimeUnit.SECONDS);

        } finally {
            pool.shutdown();
            pool.awaitTermination(2, TimeUnit.SECONDS);
            pool.shutdownNow();
            pool.awaitTermination(2, TimeUnit.SECONDS);
        }
    }

    /**
     * Tx1: lock exactly one row (and keep the lock).
     */
    private int lockOneFreeSlotWithoutNowait(UUID beltId) {
        List<?> rows = em.createNativeQuery("""
                        select id
                          from belt_slot
                         where belt_id = :beltId
                           and plate_id is null
                         order by position_index
                         for no key update
                         limit 1
                        """)
                .setParameter("beltId", beltId)
                .getResultList();

        return rows.size(); // should be 1
    }

    /**
     * Tx2: try lock the same row NOWAIT (fails immediately if Tx1 holds it).
     */
    private void lockOneFreeSlotWithNowait(UUID beltId) {
        em.createNativeQuery("""
                        select id
                          from belt_slot
                         where belt_id = :beltId
                           and plate_id is null
                         order by position_index
                         for no key update nowait
                         limit 1
                        """)
                .setParameter("beltId", beltId)
                .getResultList();
    }

    @SuppressWarnings("SameParameterValue")
    private void seedBeltAndSlotsCommitted(UUID beltId, int slotCount) {
        TransactionTemplate seedTx = new TransactionTemplate(txManager);

        seedTx.execute(status -> {
            em.createNativeQuery("""
                            insert into belt (id, name, slot_count, base_rotation_offset, offset_started_at, tick_interval_ms, speed_slots_per_tick)
                            values (:id, :name, :slotCount, 0, :startedAt, 1000, 1)
                            """)
                    .setParameter("id", beltId)
                    .setParameter("name", "LockTestBelt")
                    .setParameter("slotCount", slotCount)
                    .setParameter("startedAt", Instant.now())
                    .executeUpdate();

            for (int i = 0; i < slotCount; i++) {
                UUID slotId = UUID.nameUUIDFromBytes((beltId + ":SLOT:" + i).getBytes(StandardCharsets.UTF_8));
                em.createNativeQuery("""
                                insert into belt_slot (id, belt_id, position_index, plate_id)
                                values (:id, :beltId, :pos, null)
                                """)
                        .setParameter("id", slotId)
                        .setParameter("beltId", beltId)
                        .setParameter("pos", i)
                        .executeUpdate();
            }

            em.flush();
            return null;
        });

        em.clear();
    }

    private static void await(CountDownLatch latch, long time, TimeUnit unit) {
        try {
            latch.await(time, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static boolean hasCauseNamed(Throwable ex, String simpleName) {
        Throwable cur = ex;
        while (cur != null) {
            if (cur.getClass().getSimpleName().equals(simpleName)) return true;
            cur = cur.getCause();
        }
        return false;
    }

    /**
     * Postgres NOWAIT typically throws SQLSTATE 55P03 ("lock_not_available").
     */
    private static boolean hasPgLockNotAvailable(Throwable ex) {
        Throwable cur = ex;
        while (cur != null) {
            String msg = cur.getMessage();
            if (msg != null && (msg.contains("55P03")
                    || msg.toLowerCase().contains("lock_not_available")
                    || msg.toLowerCase().contains("could not obtain lock"))) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }
}