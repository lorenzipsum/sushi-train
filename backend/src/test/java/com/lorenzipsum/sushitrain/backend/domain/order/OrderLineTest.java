package com.lorenzipsum.sushitrain.backend.domain.order;

import com.lorenzipsum.sushitrain.backend.domain.TestData;
import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class OrderLineTest {

    private final Order order = Order.open(TestData.defaultSeat());
    private final Plate plate = TestData.plateSalmonNigiri();

    @Test
    @DisplayName("Order line can be created with sane defaults")
    void create_ok() {
        var before = Instant.now();
        var orderLine = OrderLine.create(plate, order, 450);
        var after = Instant.now();

        assertAll("Asserting sane defaults for order line",
                () -> assertNotNull(orderLine.getId()),
                () -> assertSame(plate, orderLine.getPlate()),
                () -> assertSame(order, orderLine.getOrder()),
                () -> assertEquals("Salmon Nigiri", orderLine.getMenuItemNameSnapshot()),
                () -> assertEquals(PlateTier.GREEN, orderLine.getTierSnapshot()),
                () -> assertEquals(MoneyYen.of(450), orderLine.getPriceAtPick()),
                () -> assertTrue(!orderLine.getPickedAt().isBefore(before)
                                && !orderLine.getPickedAt().isAfter(after),
                        "pickedAt should be between 'before' and 'after'")
        );
    }

    @Test
    @DisplayName("Zero price is allowed (comped/discounted)")
    void zero_price_allowed() {
        var line = OrderLine.create(plate, order, 0);
        assertEquals(MoneyYen.of(0), line.getPriceAtPick());
    }

    @Test
    @DisplayName("Order line handles null for instantiation")
    void create_not_ok() {

        assertAll("Instantiation handles null values",
                () -> assertThrows(IllegalArgumentException.class, () -> OrderLine.create(null, order, 450)),
                () -> assertThrows(IllegalArgumentException.class, () -> OrderLine.create(plate, null, 450)),
                () -> assertThrows(IllegalArgumentException.class, () -> OrderLine.create(plate, order, -450))
        );
    }

    @Test
    @DisplayName("Order line keeps snapshots ")
    void create_snapshots_ok() {
        var specialPlate = Plate.create(TestData.menuItemSalmonNigiri(), PlateTier.RED, MoneyYen.of(250), TestData.soon());

        var orderLine1 = OrderLine.create(specialPlate, order, specialPlate.getPriceAtCreation().getAmount());
        assertAll("Asserting sane defaults for order line 1",
                () -> assertEquals("Salmon Nigiri", orderLine1.getMenuItemNameSnapshot()),
                () -> assertEquals(PlateTier.RED, orderLine1.getTierSnapshot()),
                () -> assertEquals(MoneyYen.of(250), orderLine1.getPriceAtPick())
        );

        var orderLine2 = OrderLine.create(specialPlate, order, 550);
        assertEquals(MoneyYen.of(550), orderLine2.getPriceAtPick());
    }
}