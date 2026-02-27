package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.query;

import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter.JpaBaseRepositoryIT;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.projection.BeltSlotPlateRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static com.lorenzipsum.sushitrain.backend.testutil.TestFixtures.MAIN_BELT_ID;
import static org.assertj.core.api.Assertions.assertThat;

class BeltJpaQueryIT extends JpaBaseRepositoryIT {

    @Autowired
    private BeltJpaQuery beltJpaQuery;

    @Test
    @DisplayName("Snapshot returns all slots for belt, ordered by positionIndex, plate nullable")
    void findBeltSnapshot_allSlots_ordered_plateNullable() {
        // when
        List<BeltSlotPlateRow> rows = beltJpaQuery.findBeltSnapshot(MAIN_BELT_ID);

        // then
        assertThat(rows).as("rows").isNotNull().isNotEmpty();

        // all rows belong to belt
        assertThat(rows)
                .extracting(BeltSlotPlateRow::beltId)
                .as("beltId")
                .containsOnly(MAIN_BELT_ID);

        // size equals slotCount (seed contract; if you later change seed/slotCount, update here)
        assertThat(rows)
                .as("row count == slotCount")
                .hasSize(192);

        // ordered by slotPositionIndex and starts at 0 (typical belt indexing)
        assertThat(rows.getFirst().slotPositionIndex()).as("first.positionIndex").isEqualTo(0);
        assertThat(rows.getLast().slotPositionIndex()).as("last.positionIndex").isEqualTo(191);

        // monotonic increasing by 1 (strict ordering)
        for (int i = 0; i < rows.size(); i++) {
            assertThat(rows.get(i).slotPositionIndex())
                    .as("rows[%s].positionIndex".formatted(i))
                    .isEqualTo(i);
        }

        // belt scalars are consistent across rows (projection repeats them)
        var first = rows.getFirst();
        assertThat(rows)
                .allSatisfy(r -> {
                    assertThat(r.beltName()).as("beltName").isEqualTo(first.beltName());
                    assertThat(r.beltSlotCount()).as("beltSlotCount").isEqualTo(first.beltSlotCount());
                    assertThat(r.beltBaseRotationOffset()).as("beltBaseRotationOffset").isEqualTo(first.beltBaseRotationOffset());
                    assertThat(r.beltOffsetStartedAt()).as("beltOffsetStartedAt").isEqualTo(first.beltOffsetStartedAt());
                    assertThat(r.beltTickIntervalMs()).as("beltTickIntervalMs").isEqualTo(first.beltTickIntervalMs());
                    assertThat(r.beltSpeedSlotsPerTick()).as("beltSpeedSlotsPerTick").isEqualTo(first.beltSpeedSlotsPerTick());
                });

        // plate is allowed to be null (at least one should be null in seeded data or after migrations)
        // If your seed data always assigns plates, you can remove this expectation.
        boolean hasNullPlate = rows.stream().anyMatch(r -> r.plateId() == null);
        assertThat(hasNullPlate).as("at least one slot without plate").isTrue();

        // If plateId is null, then dependent plate fields must be null too (consistency)
        assertThat(rows).allSatisfy(r -> {
            if (r.plateId() == null) {
                assertThat(r.menuItemId()).as("menuItemId null when no plate").isNull();
                assertThat(r.plateTier()).as("plateTier null when no plate").isNull();
                assertThat(r.platePriceAtCreation()).as("platePriceAtCreation null when no plate").isNull();
                assertThat(r.plateStatus()).as("plateStatus null when no plate").isNull();
                assertThat(r.plateExpiresAt()).as("plateExpiresAt null when no plate").isNull();
            }
        });
    }

    @Test
    @DisplayName("Snapshot for unknown belt returns empty list")
    void findBeltSnapshot_unknownBelt_empty() {
        UUID unknown = UUID.fromString("00000000-0000-0000-0000-000000000001");

        List<BeltSlotPlateRow> rows = beltJpaQuery.findBeltSnapshot(unknown);

        assertThat(rows).as("rows").isNotNull().isEmpty();
    }
}