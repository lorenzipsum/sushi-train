package com.lorenzipsum.sushitrain.backend.domain.plate;

import com.lorenzipsum.sushitrain.backend.domain.common.YenAmount;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.exception.IllegalPlateStateException;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static com.lorenzipsum.sushitrain.backend.testutil.TestFixtures.MAGURO_NIGIRI;
import static com.lorenzipsum.sushitrain.backend.testutil.TestFixtures.inTwoHours;
import static org.junit.jupiter.api.Assertions.*;

class PlateTest {
    private final MenuItem menuItem = MenuItem.create(MAGURO_NIGIRI, PlateTier.GREEN, YenAmount.of(500));

    @Test
    @DisplayName("Plate can be created with sane defaults")
    void create_simple_ok() {
        Instant before = Instant.now();
        Instant expirationDate = inTwoHours();
        var plate = Plate.create(menuItem.getId(), menuItem.getDefaultTier(), menuItem.getBasePrice(), expirationDate);
        Instant after = Instant.now();

        assertAll("Asserting sane defaults for Plate",
                () -> assertNotNull(plate.getId()),
                () -> assertEquals(menuItem.getId(), plate.getMenuItemId()),
                () -> assertEquals(PlateTier.GREEN, plate.getTierSnapshot()),
                () -> assertEquals(YenAmount.of(500), plate.getPriceAtCreation()),
                () -> assertTrue(!plate.getCreatedAt().isBefore(before)
                                && !plate.getCreatedAt().isAfter(after),
                        "createdAt is between 'before' and 'after'"),
                () -> assertEquals(expirationDate, plate.getExpiresAt()),
                () -> assertEquals(PlateStatus.CREATED, plate.getStatus())
        );
    }

    @Test
    @DisplayName("Plate can be created with overwritten defaults")
    void create_overwrite_defaults_ok() {
        var plate = Plate.create(menuItem.getId(), PlateTier.RED, YenAmount.of(666), inTwoHours());

        assertAll("Asserting defaults can be overwritten",
                () -> assertEquals(PlateTier.RED, plate.getTierSnapshot()),
                () -> assertEquals(YenAmount.of(666), plate.getPriceAtCreation())
        );
    }

    @Test
    @DisplayName("Plate creation handles null values correctly")
    void create_not_ok() {
        assertAll("Asserting Plate handles null values",
                () -> assertThrows(IllegalArgumentException.class, () -> Plate.create(null, PlateTier.RED, YenAmount.of(500), inTwoHours())),
                () -> assertThrows(IllegalArgumentException.class, () -> Plate.create(menuItem.getId(), null, YenAmount.of(500), inTwoHours())),
                () -> assertThrows(IllegalArgumentException.class, () -> Plate.create(menuItem.getId(), PlateTier.RED, null, inTwoHours())),
                () -> assertThrows(IllegalArgumentException.class, () -> Plate.create(menuItem.getId(), PlateTier.RED, YenAmount.of(500), null))
        );
    }

    @Test
    @DisplayName("Expiration must be in the future")
    void create_expiration_in_future() {
        var past = Instant.now().minusSeconds(1);
        assertThrows(IllegalArgumentException.class,
                () -> Plate.create(menuItem.getId(), PlateTier.RED, YenAmount.of(500), past));
    }

    @Test
    @DisplayName("Plate expires correctly")
    void expire_ok() {
        // given
        var plate = Plate.create(menuItem.getId(), menuItem.getDefaultTier(), menuItem.getBasePrice(), inTwoHours());

        // when
        plate.expire();

        // then
        assertEquals(PlateStatus.EXPIRED, plate.getStatus());
    }

    @Test
    @DisplayName("expire() is idempotent")
    void expire_idempotent() {
        var plate = Plate.create(menuItem.getId(), menuItem.getDefaultTier(), menuItem.getBasePrice(), inTwoHours());
        plate.expire();
        plate.expire();
        assertEquals(PlateStatus.EXPIRED, plate.getStatus());
    }

    @Test
    @DisplayName("Cannot expire a picked plate")
    void expire_picked_not_ok() {
        var plate = Plate.create(menuItem.getId(), menuItem.getDefaultTier(), menuItem.getBasePrice(), inTwoHours());
        plate.pick();
        assertThrows(IllegalPlateStateException.class, plate::expire);
    }

    @Test
    void pick_ok() {
        var plate = Plate.create(menuItem.getId(), menuItem.getDefaultTier(), menuItem.getBasePrice(), inTwoHours());
        plate.pick();
        assertEquals(PlateStatus.PICKED, plate.getStatus());
    }

    @Test
    void pick_idempotent() {
        var plate = Plate.create(menuItem.getId(), menuItem.getDefaultTier(), menuItem.getBasePrice(), inTwoHours());
        plate.pick();
        plate.pick();
        assertEquals(PlateStatus.PICKED, plate.getStatus());
    }

    @Test
    void pick_expired_not_ok() {
        var plate = Plate.create(menuItem.getId(), menuItem.getDefaultTier(), menuItem.getBasePrice(), inTwoHours());
        plate.expire();
        assertThrows(IllegalPlateStateException.class, plate::pick);
    }

    @Test
    void place_ok() {
        var plate = Plate.create(menuItem.getId(), menuItem.getDefaultTier(), menuItem.getBasePrice(), inTwoHours());
        plate.place();
        assertEquals(PlateStatus.ON_BELT, plate.getStatus());
    }

    @Test
    void place_idempotent() {
        var plate = Plate.create(menuItem.getId(), menuItem.getDefaultTier(), menuItem.getBasePrice(), inTwoHours());
        plate.place();
        plate.place();
        assertEquals(PlateStatus.ON_BELT, plate.getStatus());
    }

    @Test
    void place_expired_not_ok() {
        var plate = Plate.create(menuItem.getId(), menuItem.getDefaultTier(), menuItem.getBasePrice(), inTwoHours());
        plate.expire();
        assertThrows(IllegalPlateStateException.class, plate::place);
    }


    @Test
    void place_picked_not_ok() {
        var plate = Plate.create(menuItem.getId(), menuItem.getDefaultTier(), menuItem.getBasePrice(), inTwoHours());
        plate.pick();
        assertThrows(IllegalPlateStateException.class, plate::place);
    }
}