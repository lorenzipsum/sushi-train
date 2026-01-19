package com.lorenzipsum.sushitrain.backend.domain.plate;

import com.lorenzipsum.sushitrain.backend.TestData;
import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PlateTest {
    private final Instant inTwoHours = TestData.inTwoHours();
    private final MenuItem menuItem = TestData.menuItemMaguroNigiri();

    @Test
    @DisplayName("Plate can be created with sane defaults")
    void create_simple_ok() {
        var menuItem = TestData.menuItemMaguroNigiri();
        Instant before = Instant.now();
        var plate = Plate.create(menuItem.getId(), menuItem.getDefaultTier(), menuItem.getBasePrice(), inTwoHours);
        Instant after = Instant.now();

        assertAll("Asserting sane defaults for Plate",
                () -> assertNotNull(plate.getId()),
                () -> assertEquals(menuItem.getId(), plate.getMenuItemId()),
                () -> assertEquals(PlateTier.GREEN, plate.getTierSnapshot()),
                () -> assertEquals(MoneyYen.of(500), plate.getPriceAtCreation()),
                () -> assertTrue(!plate.getCreatedAt().isBefore(before)
                                && !plate.getCreatedAt().isAfter(after),
                        "createdAt is between 'before' and 'after'"),
                () -> assertEquals(inTwoHours, plate.getExpiresAt()),
                () -> assertEquals(PlateStatus.ON_BELT, plate.getStatus())
        );
    }

    @Test
    @DisplayName("Plate can be created with overwritten defaults")
    void create_overwrite_defaults_ok() {
        var plate = Plate.create(menuItem.getId(), PlateTier.RED, MoneyYen.of(666), inTwoHours);

        assertAll("Asserting defaults can be overwritten",
                () -> assertEquals(PlateTier.RED, plate.getTierSnapshot()),
                () -> assertEquals(MoneyYen.of(666), plate.getPriceAtCreation())
        );
    }

    @Test
    @DisplayName("Plate creation handles null values correctly")
    void create_not_ok() {
        assertAll("Asserting Plate handles null values",
                () -> assertThrows(IllegalArgumentException.class, () -> Plate.create(null, PlateTier.RED, MoneyYen.of(500), inTwoHours)),
                () -> assertThrows(IllegalArgumentException.class, () -> Plate.create(menuItem.getId(), null, MoneyYen.of(500), inTwoHours)),
                () -> assertThrows(IllegalArgumentException.class, () -> Plate.create(menuItem.getId(), PlateTier.RED, null, inTwoHours)),
                () -> assertThrows(IllegalArgumentException.class, () -> Plate.create(menuItem.getId(), PlateTier.RED, MoneyYen.of(500), null))
        );
    }

    @Test
    @DisplayName("Expiration must be in the future")
    void create_expiration_in_future() {
        var past = Instant.now().minusSeconds(1);
        assertThrows(IllegalArgumentException.class,
                () -> Plate.create(menuItem.getId(), PlateTier.RED, MoneyYen.of(500), past));
    }

    @Test
    @DisplayName("Plate expires correctly")
    void expire_ok() {
        // given
        var plate = Plate.create(menuItem.getId(), menuItem.getDefaultTier(), menuItem.getBasePrice(), inTwoHours);

        // when
        plate.expire();

        // then
        assertEquals(PlateStatus.EXPIRED, plate.getStatus());
    }

    @Test
    @DisplayName("expire() is idempotent")
    void expire_idempotent() {
        var plate = Plate.create(menuItem.getId(), menuItem.getDefaultTier(), menuItem.getBasePrice(), inTwoHours);
        plate.expire();
        plate.expire();
        assertEquals(PlateStatus.EXPIRED, plate.getStatus());
    }
}