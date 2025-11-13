package com.lorenzipsum.sushitrain.backend.domain.order;

import com.lorenzipsum.sushitrain.backend.domain.TestData;
import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static com.lorenzipsum.sushitrain.backend.domain.TestData.SALMON_NIGIRI;
import static org.junit.jupiter.api.Assertions.*;

class OrderLineTest {

    private final Order order = Order.open(TestData.defaultSeat());
    private final Plate plate = TestData.plateSalmonNigiri();

    @Test
    @DisplayName("Order line can be created with sane defaults")
    void create_ok() {
        var before = Instant.now();
        var orderLine = OrderLine.create(plate, order, SALMON_NIGIRI, 450);
        var after = Instant.now();

        assertAll("Asserting sane defaults for order line",
                () -> assertNotNull(orderLine.getId()),
                () -> assertSame(plate, orderLine.getPlate()),
                () -> assertSame(order, orderLine.getOrder()),
                () -> assertEquals(SALMON_NIGIRI, orderLine.getMenuItemNameSnapshot()),
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
        var line = OrderLine.create(plate, order, SALMON_NIGIRI, 0);
        assertEquals(MoneyYen.of(0), line.getPriceAtPick());
    }

    @Test
    @DisplayName("Order line handles null for instantiation")
    void create_not_ok() {

        assertAll("Instantiation handles null values",
                () -> assertThrows(IllegalArgumentException.class, () -> OrderLine.create(null, order, SALMON_NIGIRI, 450)),
                () -> assertThrows(IllegalArgumentException.class, () -> OrderLine.create(plate, null, SALMON_NIGIRI, 450)),
                () -> assertThrows(IllegalArgumentException.class, () -> OrderLine.create(plate, order, SALMON_NIGIRI, -450))
        );
    }

    @Test
    @DisplayName("Order line keeps snapshots ")
    void create_snapshots_ok() {
        var specialPlate = Plate.create(UUID.randomUUID(), PlateTier.RED, MoneyYen.of(250), TestData.inTwoHours());

        var orderLine1 = OrderLine.create(specialPlate, order, SALMON_NIGIRI, specialPlate.getPriceAtCreation().getAmount());
        assertAll("Asserting sane defaults for order line 1",
                () -> assertEquals(SALMON_NIGIRI, orderLine1.getMenuItemNameSnapshot()),
                () -> assertEquals(PlateTier.RED, orderLine1.getTierSnapshot()),
                () -> assertEquals(MoneyYen.of(250), orderLine1.getPriceAtPick())
        );

        var orderLine2 = OrderLine.create(specialPlate, order, SALMON_NIGIRI, 550);
        assertEquals(MoneyYen.of(550), orderLine2.getPriceAtPick());
    }
}