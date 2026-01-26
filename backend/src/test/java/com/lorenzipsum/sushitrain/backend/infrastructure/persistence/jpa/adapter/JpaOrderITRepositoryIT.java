package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.TestData;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltRepository;
import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.order.Order;
import com.lorenzipsum.sushitrain.backend.domain.order.OrderRepository;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.domain.plate.PlateRepository;
import com.lorenzipsum.sushitrain.backend.domain.seat.Seat;
import com.lorenzipsum.sushitrain.backend.domain.seat.SeatRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static com.lorenzipsum.sushitrain.backend.TestData.MENU_ITEM_DEFAULT_ID;
import static com.lorenzipsum.sushitrain.backend.TestData.inTwoHours;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Import({JpaOrderRepository.class,
        JpaBeltITRepositoryIT.class,
        JpaSeatRepository.class,
        JpaPlateRepository.class,
        OrderMapper.class,
        OrderLineMapper.class,
        BeltMapper.class,
        BeltSlotMapper.class,
        SeatMapper.class,
        PlateMapper.class})
class JpaOrderITRepositoryIT extends JpaBaseRepositoryIT {

    @Autowired
    private OrderRepository repository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private BeltRepository beltRepository;
    @Autowired
    private PlateRepository plateRepository;


    @Test
    @DisplayName("persist and load a Order via adapter")
    void persistAndLoadOrders_ok() {
        // Arrange
        var belt = beltRepository.save(TestData.newBelt());
        var seat = seatRepository.save(Seat.create("A1", belt.getId(), 1));

        // Act
        var order = Order.open(seat.getId());
        var savedOrder = repository.save(order);
        em.flush();
        em.clear();
        var reloadedOpt = repository.findById(order.getId());

        // Assert
        assertThat(order.getId()).isNotNull();
        assertThat(reloadedOpt).isPresent();

        var reloaded = reloadedOpt.get();

        assertAll("Asserting reloaded values correct",
                () -> assertEquals(order.getId(), savedOrder.getId()),
                () -> assertEquals(order.getId(), reloaded.getId()),
                () -> assertEquals(order.getSeatId(), reloaded.getSeatId()),
                () -> assertEquals(order.getStatus(), reloaded.getStatus()),
                () -> assertEquals(order.getCreatedAt().truncatedTo(ChronoUnit.MILLIS), reloaded.getCreatedAt().truncatedTo(ChronoUnit.MILLIS)),
                () -> assertEquals(order.getLines().size(), reloaded.getLines().size()));
    }

    @Test
    @DisplayName("persist and load a Order via adapter")
    void persistAndLoadOrders_containing_line_ok() {
        // Arrange
        var belt = beltRepository.save(TestData.newBelt());
        var seat = seatRepository.save(Seat.create("A1", belt.getId(), 1));
        var plate = plateRepository.save(Plate.create(MENU_ITEM_DEFAULT_ID, PlateTier.RED, MoneyYen.of(400), inTwoHours()));

        // Act
        var order = Order.open(seat.getId());
        var line = order.addLineFromPlate(plate.getId(), "Salmon Nigiri", PlateTier.GREEN, 400);

        var savedOrder = repository.save(order);
        em.flush();
        em.clear();
        var reloadedOpt = repository.findById(savedOrder.getId());

        // Assert
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(reloadedOpt).isPresent();

        var reloaded = reloadedOpt.get();
        var reloadedLine = reloaded.getLines().getFirst();

        assertAll("Asserting reloaded values correct",
                () -> assertEquals(order.getId(), savedOrder.getId()),
                () -> assertEquals(order.getId(), reloaded.getId()),
                () -> assertEquals(order.getSeatId(), reloaded.getSeatId()),
                () -> assertEquals(order.getStatus(), reloaded.getStatus()),
                () -> assertEquals(order.getCreatedAt().truncatedTo(ChronoUnit.MILLIS), reloaded.getCreatedAt().truncatedTo(ChronoUnit.MILLIS)),
                () -> assertEquals(order.getLines().size(), reloaded.getLines().size()),
                () -> assertEquals(line.getId(), reloadedLine.getId()),
                () -> assertEquals(line.getOrderId(), reloadedLine.getOrderId()),
                () -> assertEquals(order.getId(), reloadedLine.getOrderId()),
                () -> assertEquals(line.getPlateId(), reloadedLine.getPlateId()),
                () -> assertEquals(line.getPriceAtPick(), reloadedLine.getPriceAtPick()),
                () -> assertEquals(line.getTierSnapshot(), reloadedLine.getTierSnapshot()),
                () -> assertEquals(line.getMenuItemNameSnapshot(), reloadedLine.getMenuItemNameSnapshot())
        );
    }


    @Test
    @DisplayName("persist and load a Order via adapter")
    void persistAndLoadOrders_line_manipulations_ok() {
        // Arrange
        var belt = beltRepository.save(TestData.newBelt());
        var seat = seatRepository.save(Seat.create("A1", belt.getId(), 1));
        var plate = plateRepository.save(Plate.create(MENU_ITEM_DEFAULT_ID, PlateTier.RED, MoneyYen.of(400), inTwoHours()));
        var order = Order.open(seat.getId());
        order.addLineFromPlate(plate.getId(), "Salmon Nigiri", PlateTier.GREEN, 400);

        // Act
        var savedOrder = repository.save(order);
        em.flush();
        em.clear();
        var firstReload = repository.findById(order.getId()).orElseThrow(
                () -> new IllegalArgumentException("Order not found"));

        // Assert
        assertFalse(firstReload.getLines().isEmpty());

        savedOrder.removeLine(savedOrder.getLines().getFirst());
        repository.save(savedOrder);
        em.flush();
        em.clear();

        var secondReload = repository.findById(order.getId()).orElseThrow(
                () -> new IllegalArgumentException("Order not found"));

        assertTrue(secondReload.getLines().isEmpty());
    }


    @Test
    void saveFailsWhenSeatMissing() {
        var seat = TestData.newSeatWithNewBelt();
        var order = Order.open(seat.getId());

        repository.save(order);

        assertThrows(Exception.class, em::flush);
    }

    @Test
    @DisplayName("persist checks for null values")
    void persistAndLoadOrders_not_ok() {
        assertAll("Asserting null handling",
                () -> assertThrows(IllegalArgumentException.class, () -> repository.save(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> repository.findById(null)),
                () -> assertFalse(repository.findById(UUID.randomUUID()).isPresent())
        );
    }
}