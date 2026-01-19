package com.lorenzipsum.sushitrain.backend;

import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItem;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.domain.seat.Seat;

import java.time.Instant;
import java.util.UUID;

public final class TestData {
    public static final String SALMON_NIGIRI = "Salmon Nigiri";
    public static final String CHICKEN_KARAAGE = "Chicken Karaage";
    public static final String MAGURO_NIGIRI = "Maguro Nigiri";
    public static final UUID MENU_ITEM_DEFAULT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private TestData() {
    }

    public static Belt newBelt() {
        return Belt.create("Default", 10);
    }

    public static MenuItem menuItemSalmonNigiri() {
        return MenuItem.create(SALMON_NIGIRI, PlateTier.GREEN, new MoneyYen(450));
    }

    public static MenuItem menuItemMaguroNigiri() {
        return MenuItem.create(MAGURO_NIGIRI, PlateTier.GREEN, MoneyYen.of(500));
    }

    public static Plate plateSalmonNigiri() {
        return Plate.create(UUID.randomUUID(), PlateTier.GREEN, new MoneyYen(450), inTwoHours());
    }

    public static Plate plateChickenKaraage() {
        return Plate.create(UUID.randomUUID(), PlateTier.GOLD, MoneyYen.of(800), inTwoHours());
    }

    public static Seat newSeatWithNewBelt() {
        return Seat.create("1", newBelt().getId(), 5);
    }

    public static Instant inTwoHours() {
        return Instant.now().plusSeconds(7200);
    }
}
