package com.lorenzipsum.sushitrain.backend.domain;

import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItem;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.domain.seat.Seat;

import java.time.Instant;

public final class TestData {
    private TestData() {
    }

    public static Belt defaultBelt() {
        return Belt.create("Default", 10);
    }

    public static MenuItem menuItemSalmonNigiri() {
        return MenuItem.create("Salmon Nigiri", PlateTier.GREEN, new MoneyYen(450));
    }

    public static MenuItem menuItemChickenKaraage() {
        return MenuItem.create("Chicken Karaage", PlateTier.GOLD, MoneyYen.of(800));
    }

    public static MenuItem menuItemMaguroNigiri() {
        return MenuItem.create("Maguro Nigiri", PlateTier.GREEN, MoneyYen.of(500));
    }

    public static Plate plateSalmonNigiri() {
        return Plate.create(menuItemSalmonNigiri(), soon());
    }

    public static Plate plateChickenKaraage() {
        return Plate.create(menuItemChickenKaraage(), soon());
    }

    public static Seat defaultSeat() {
        return Seat.create("1", defaultBelt(), 5);
    }

    public static Instant soon() {
        return Instant.now().plusSeconds(7200);
    }
}
