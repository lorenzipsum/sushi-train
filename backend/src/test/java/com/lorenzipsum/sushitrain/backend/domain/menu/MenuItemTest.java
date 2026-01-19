package com.lorenzipsum.sushitrain.backend.domain.menu;

import com.lorenzipsum.sushitrain.backend.TestData;
import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static com.lorenzipsum.sushitrain.backend.TestData.SALMON_NIGIRI;
import static org.junit.jupiter.api.Assertions.*;

class MenuItemTest {

    @Test
    @DisplayName("Menu item can be created with sane defaults")
    void create_ok() {
        var before = Instant.now();
        var salmonNigiri = TestData.menuItemSalmonNigiri();
        var after = Instant.now();

        assertAll("Asserting instantiated menu item",
                () -> assertNotNull(salmonNigiri.getId()),
                () -> assertEquals(SALMON_NIGIRI, salmonNigiri.getName()),
                () -> assertEquals(PlateTier.GREEN, salmonNigiri.getDefaultTier()),
                () -> assertEquals(MoneyYen.of(450), salmonNigiri.getBasePrice()),
                () -> assertEquals(450, salmonNigiri.getBasePrice().amount()),
                () -> assertNotNull(salmonNigiri.getCreatedAt()),
                () -> assertTrue(!salmonNigiri.getCreatedAt().isBefore(before) &&
                                !salmonNigiri.getCreatedAt().isAfter(after),
                        "createdAt should be between 'before' and 'after'")
        );
    }

    @Test
    @DisplayName("Menu item cannot be created with null values")
    void create_not_ok() {
        assertAll("Asserting correct behaviour instantiating with null values",
                () -> assertThrows(IllegalArgumentException.class, () -> MenuItem.create(null, PlateTier.GREEN, MoneyYen.of(500))),
                () -> assertThrows(IllegalArgumentException.class, () -> MenuItem.create(SALMON_NIGIRI, null, MoneyYen.of(500))),
                () -> assertThrows(IllegalArgumentException.class, () -> MenuItem.create(SALMON_NIGIRI, PlateTier.GREEN, null)),
                () -> assertThrows(IllegalArgumentException.class, () -> MenuItem.create("", PlateTier.GREEN, MoneyYen.of(500))),
                () -> assertThrows(IllegalArgumentException.class, () -> MenuItem.create("   ", PlateTier.GREEN, MoneyYen.of(500)))
        );
    }

    @Test
    @DisplayName("Name is trimmed on creation")
    void create_trims_name() {
        var item = MenuItem.create("  Tuna  ", PlateTier.GREEN, MoneyYen.of(300));
        assertEquals("Tuna", item.getName());
    }

    @Test
    @DisplayName("Two created items get different IDs")
    void create_ids_unique() {
        var a = MenuItem.create("A", PlateTier.GREEN, MoneyYen.of(100));
        var b = MenuItem.create("B", PlateTier.GREEN, MoneyYen.of(100));
        assertNotEquals(a.getId(), b.getId());
    }
}