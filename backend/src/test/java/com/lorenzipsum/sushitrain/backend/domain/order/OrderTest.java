package com.lorenzipsum.sushitrain.backend.domain.order;

import com.lorenzipsum.sushitrain.backend.TestData;
import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.OrderStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.domain.seat.Seat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static com.lorenzipsum.sushitrain.backend.TestData.CHICKEN_KARAAGE;
import static com.lorenzipsum.sushitrain.backend.TestData.SALMON_NIGIRI;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private final Seat seat = TestData.newSeatWithNewBelt();
    private final Plate chickenKaraagePlate = TestData.plateChickenKaraage();
    private final Plate salmonNigiriPlate = TestData.plateSalmonNigiri();

    @Test
    @DisplayName("Order is opened with sane defaults")
    void open_ok() {
        var before = Instant.now();
        var order = Order.open(seat.getId());
        var after = Instant.now();

        assertAll("Asserting sane defaults for order",
                () -> assertNotNull(order.getId()),
                () -> assertEquals(seat.getId(), order.getSeatId()),
                () -> assertEquals(0, order.getLines().size()),
                () -> assertEquals(OrderStatus.OPEN, order.getStatus()),
                () -> assertTrue(!order.getCreatedAt().isBefore(before)
                                && !order.getCreatedAt().isAfter(after),
                        "createdAt is between 'before' and 'after'"),
                () -> assertNull(order.getClosedAt())
        );
    }

    @Test
    @DisplayName("Order can not be opened if seat is null")
    void open_not_ok() {
        assertThrows(IllegalArgumentException.class, () -> Order.open(null));
    }

    @Test
    @DisplayName("Line can be added by plate")
    void addLineFromPlate_ok() {
        var order = Order.open(seat.getId());

        var before = Instant.now();
        var orderLine = order.addLineFromPlate(chickenKaraagePlate.getId(), CHICKEN_KARAAGE, PlateTier.GOLD, 800);
        var after = Instant.now();

        assertAll("Asserting sane defaults for order line",
                () -> assertNotNull(orderLine.getId()),
                () -> assertEquals(chickenKaraagePlate.getId(), orderLine.getPlateId()),
                () -> assertEquals(order.getId(), orderLine.getOrderId()),
                () -> assertEquals(CHICKEN_KARAAGE, orderLine.getMenuItemNameSnapshot()),
                () -> assertEquals(PlateTier.GOLD, orderLine.getTierSnapshot()),
                () -> assertEquals(MoneyYen.of(800), orderLine.getPriceAtPick()),
                () -> assertTrue(!orderLine.getPickedAt().isBefore(before)
                                && !orderLine.getPickedAt().isAfter(after),
                        "pickedAt should be between 'before' and 'after'")
        );

        assertAll("Asserting order after adding via addLineFromPlate",
                () -> assertEquals(1, order.getLines().size()),
                () -> assertSame(orderLine, order.getLines().getFirst()));
    }

    @Test
    @DisplayName("Line cannot be added with null")
    void addLineFromPlate_not_ok() {
        var order = Order.open(seat.getId());

        assertAll("Asserting correct null handling",
                () -> assertThrows(IllegalArgumentException.class, () -> order.addLineFromPlate(null, SALMON_NIGIRI, PlateTier.GREEN, 500)),
                () -> assertThrows(IllegalArgumentException.class, () -> order.addLineFromPlate(null, null, PlateTier.GREEN, 500)),
                () -> assertThrows(IllegalArgumentException.class, () -> order.addLineFromPlate(null, "", PlateTier.GREEN, 500)),
                () -> assertThrows(IllegalArgumentException.class, () -> order.addLineFromPlate(null, " ", PlateTier.GREEN, 500)),
                () -> assertThrows(IllegalArgumentException.class, () -> order.addLineFromPlate(salmonNigiriPlate.getId(), SALMON_NIGIRI, PlateTier.GREEN, -5))
        );
    }

    @Test
    @DisplayName("Lines list is unmodifiable from the outside")
    void lines_are_unmodifiable() {
        var order = Order.open(seat.getId());
        assertThrows(UnsupportedOperationException.class, () -> order.getLines().add(null));
    }

    @Test
    @DisplayName("Total cost is correct")
    void total_ok() {
        var order = Order.open(seat.getId());

        var orderLine1 = order.addLineFromPlate(chickenKaraagePlate.getId(), CHICKEN_KARAAGE, PlateTier.GOLD, chickenKaraagePlate.getPriceAtCreation().amount());
        var orderLine2 = order.addLineFromPlate(salmonNigiriPlate.getId(), SALMON_NIGIRI, PlateTier.GREEN, salmonNigiriPlate.getPriceAtCreation().amount());

        assertAll("Asserting order and lines",
                () -> assertEquals(1250, order.total().amount()),
                () -> assertEquals(2, order.getLines().size()),
                () -> assertSame(orderLine1, order.getLines().getFirst()),
                () -> assertSame(orderLine2, order.getLines().getLast())
        );
    }

    @Test
    @DisplayName("Checkout works correctly")
    void checkout_ok() {

        var order = Order.open(seat.getId());

        order.addLineFromPlate(chickenKaraagePlate.getId(), CHICKEN_KARAAGE, PlateTier.GOLD, chickenKaraagePlate.getPriceAtCreation().amount());

        var before = Instant.now();
        order.checkout();
        var after = Instant.now();

        assertAll("Asserting order after checkout",
                () -> assertEquals(OrderStatus.CHECKED_OUT, order.getStatus()),
                () -> assertEquals(1, order.getLines().size()),
                () -> assertEquals(800, order.total().amount()),
                () -> assertTrue(!order.getClosedAt().isBefore(before)
                                && !order.getClosedAt().isAfter(after),
                        "closedAt should be between 'before' and 'after'"));

        assertThrows(IllegalStateException.class, () ->
                order.addLineFromPlate(salmonNigiriPlate.getId(), SALMON_NIGIRI, PlateTier.GREEN, salmonNigiriPlate.getPriceAtCreation().amount()));
    }

    @Test
    @DisplayName("Checkout is not allowed twice")
    void checkout_twice_throws() {
        var order = Order.open(seat.getId());
        order.addLineFromPlate(salmonNigiriPlate.getId(), SALMON_NIGIRI, PlateTier.GREEN, 450);
        order.checkout();
        assertThrows(IllegalStateException.class, order::checkout);
    }

    @Test
    @DisplayName("Zero price is allowed")
    void zero_price_allowed() {
        Order order = Order.open(seat.getId());
        var line = order.addLineFromPlate(salmonNigiriPlate.getId(), SALMON_NIGIRI, PlateTier.GREEN, 0);
        assertEquals(MoneyYen.of(0), line.getPriceAtPick());
    }

    @Test
    void removeLine_ok() {
        Order order = Order.open(seat.getId());

        OrderLine orderLine1 = order.addLineFromPlate(chickenKaraagePlate.getId(), CHICKEN_KARAAGE, PlateTier.GOLD, chickenKaraagePlate.getPriceAtCreation().amount());
        OrderLine orderLine2 = order.addLineFromPlate(salmonNigiriPlate.getId(), SALMON_NIGIRI, PlateTier.GREEN, salmonNigiriPlate.getPriceAtCreation().amount());

        order.removeLine(orderLine1);

        assertAll("Removal of line is working",
                () -> assertEquals(1, order.getLines().size()),
                () -> assertSame(orderLine2, order.getLines().getFirst()),
                () -> assertEquals(MoneyYen.of(450), order.total()),
                () -> assertThrows(IllegalArgumentException.class, () -> order.removeLine(orderLine1)));

        order.checkout();

        assertThrows(IllegalStateException.class, () -> order.removeLine(orderLine2));
    }

    @Test
    @DisplayName("After removing a line while OPEN, you can add another line")
    void remove_then_add_again_ok() {
        var order = Order.open(seat.getId());
        var line1 = order.addLineFromPlate(chickenKaraagePlate.getId(), CHICKEN_KARAAGE, PlateTier.GOLD, 800);
        order.removeLine(line1);
        order.addLineFromPlate(salmonNigiriPlate.getId(), SALMON_NIGIRI, PlateTier.GREEN, 450);
        assertEquals(1, order.getLines().size());
    }
}