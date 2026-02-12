package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltRepository;
import com.lorenzipsum.sushitrain.backend.domain.belt.SeatSpec;
import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.order.Order;
import com.lorenzipsum.sushitrain.backend.domain.order.OrderRepository;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.domain.plate.PlateRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static com.lorenzipsum.sushitrain.backend.testutil.TestFixtures.SALMON_NIGIRI_ID;
import static com.lorenzipsum.sushitrain.backend.testutil.TestFixtures.inTwoHours;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Import({
        JpaOrderRepository.class,
        JpaBeltRepository.class,
        JpaPlateRepository.class,
        OrderMapper.class,
        OrderLineMapper.class,
        BeltMapper.class,
        BeltSlotMapper.class,
        SeatMapper.class,
        PlateMapper.class
})
class JpaOrderRepositoryIT extends JpaBaseRepositoryIT {

    @Autowired
    private OrderRepository repository;

    @Autowired
    private BeltRepository beltRepository;

    @Autowired
    private PlateRepository plateRepository;

    @Test
    @DisplayName("persist and load an Order via adapter")
    void persistAndLoadOrders_ok() {
        // Arrange: create belt WITH seats and persist it
        var belt = beltRepository.save(Belt.create("Default", 10, List.of(new SeatSpec("A1", 1))));
        em.flush();
        em.clear();

        // reload belt to be sure seats exist in DB
        var reloadedBelt = beltRepository.findById(belt.getId()).orElseThrow();
        var seatId = reloadedBelt.getSeats().getFirst().getId();

        // Act
        var order = Order.open(seatId);
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
                () -> assertEquals(order.getLines().size(), reloaded.getLines().size())
        );
    }

    @Test
    @DisplayName("persist and load an Order containing a line")
    void persistAndLoadOrders_containing_line_ok() {
        // Arrange
        var belt = beltRepository.save(Belt.create("Default", 10, List.of(new SeatSpec("A1", 1))));
        var seatId = beltRepository.findById(belt.getId()).orElseThrow().getSeats().getFirst().getId();
        var plate = plateRepository.save(Plate.create(SALMON_NIGIRI_ID, PlateTier.RED, MoneyYen.of(400), inTwoHours()));

        // Act
        var order = Order.open(seatId);
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
                () -> assertEquals(order.getId(), reloadedLine.getOrderId()),
                () -> assertEquals(line.getPlateId(), reloadedLine.getPlateId()),
                () -> assertEquals(line.getPriceAtPick(), reloadedLine.getPriceAtPick()),
                () -> assertEquals(line.getTierSnapshot(), reloadedLine.getTierSnapshot()),
                () -> assertEquals(line.getMenuItemNameSnapshot(), reloadedLine.getMenuItemNameSnapshot())
        );
    }

    @Test
    @DisplayName("persist order and remove line, then persist again")
    void persistAndLoadOrders_line_manipulations_ok() {
        // Arrange
        var belt = beltRepository.save(Belt.create("Default", 10, List.of(new SeatSpec("A1", 1))));
        var seatId = beltRepository.findById(belt.getId()).orElseThrow().getSeats().getFirst().getId();
        var plate = plateRepository.save(Plate.create(SALMON_NIGIRI_ID, PlateTier.RED, MoneyYen.of(400), inTwoHours()));

        var order = Order.open(seatId);
        order.addLineFromPlate(plate.getId(), "Salmon Nigiri", PlateTier.GREEN, 400);

        // Act
        var savedOrder = repository.save(order);
        em.flush();
        em.clear();

        var firstReload = repository.findById(savedOrder.getId()).orElseThrow();
        assertFalse(firstReload.getLines().isEmpty());

        firstReload.removeLine(firstReload.getLines().getFirst());
        repository.save(firstReload);
        em.flush();
        em.clear();

        var secondReload = repository.findById(savedOrder.getId()).orElseThrow();
        assertTrue(secondReload.getLines().isEmpty());
    }

    @Test
    void saveFailsWhenSeatMissing() {
        // seatId that does not exist in DB
        var missingSeatId = UUID.randomUUID();
        var order = Order.open(missingSeatId);

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
