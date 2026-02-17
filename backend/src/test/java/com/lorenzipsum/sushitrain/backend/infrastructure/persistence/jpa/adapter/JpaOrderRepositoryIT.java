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
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        var belt = beltRepository.create(Belt.create("Default", 10, List.of(new SeatSpec("A1", 1))));
        em.flush();
        em.clear();
        var seatId = beltRepository.findById(belt.getId()).orElseThrow().getSeats().getFirst().getId();

        var order = Order.open(seatId);
        var savedOrder = repository.save(order);
        em.flush();
        em.clear();
        var reloadedOpt = repository.findById(order.getId());

        assertThat(order.getId()).as("order.id").isNotNull();
        assertThat(reloadedOpt).as("reloadedOpt").isPresent();

        var reloaded = reloadedOpt.orElseThrow();

        assertThat(savedOrder.getId()).as("savedOrder.id").isEqualTo(order.getId());
        assertThat(reloaded.getId()).as("reloaded.id").isEqualTo(order.getId());
        assertThat(reloaded.getSeatId()).as("reloaded.seatId").isEqualTo(order.getSeatId());
        assertThat(reloaded.getStatus()).as("reloaded.status").isEqualTo(order.getStatus());
        assertThat(reloaded.getCreatedAt()).as("reloaded.createdAt").isNotNull();
        assertThat(reloaded.getCreatedAt().truncatedTo(ChronoUnit.MILLIS)).as("reloaded.createdAt (truncatedToMillis)").isEqualTo(order.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
        assertThat(reloaded.getLines()).as("reloaded.lines").isNotNull().hasSize(order.getLines().size());
    }

    @Test
    @DisplayName("persist and load an Order containing a line")
    void persistAndLoadOrders_containing_line_ok() {
        var belt = beltRepository.create(Belt.create("Default", 10, List.of(new SeatSpec("A1", 1))));
        var seatId = beltRepository.findById(belt.getId()).orElseThrow().getSeats().getFirst().getId();
        var plate = plateRepository.save(Plate.create(SALMON_NIGIRI_ID, PlateTier.RED, MoneyYen.of(400), inTwoHours()));

        var order = Order.open(seatId);
        var line = order.addLineFromPlate(plate.getId(), "Salmon Nigiri", PlateTier.GREEN, 400);

        var savedOrder = repository.save(order);
        em.flush();
        em.clear();
        var reloadedOpt = repository.findById(savedOrder.getId());

        assertThat(savedOrder.getId()).as("savedOrder.id").isNotNull();
        assertThat(reloadedOpt).as("reloadedOpt").isPresent();

        var reloaded = reloadedOpt.orElseThrow();
        assertThat(reloaded.getLines()).as("reloaded.lines").isNotNull().isNotEmpty();
        var reloadedLine = reloaded.getLines().getFirst();

        assertThat(savedOrder.getId()).as("savedOrder.id").isEqualTo(order.getId());
        assertThat(reloaded.getId()).as("reloaded.id").isEqualTo(order.getId());
        assertThat(reloaded.getSeatId()).as("reloaded.seatId").isEqualTo(order.getSeatId());
        assertThat(reloaded.getStatus()).as("reloaded.status").isEqualTo(order.getStatus());
        assertThat(reloaded.getCreatedAt()).as("reloaded.createdAt").isNotNull();
        assertThat(reloaded.getCreatedAt().truncatedTo(ChronoUnit.MILLIS)).as("reloaded.createdAt (truncatedToMillis)").isEqualTo(order.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
        assertThat(reloaded.getLines()).as("reloaded.lines size").hasSize(order.getLines().size());

        assertThat(reloadedLine.getId()).as("line.id").isEqualTo(line.getId());
        assertThat(reloadedLine.getOrderId()).as("line.orderId").isEqualTo(order.getId());
        assertThat(reloadedLine.getPlateId()).as("line.plateId").isEqualTo(line.getPlateId());
        assertThat(reloadedLine.getPriceAtPick()).as("line.priceAtPick").isEqualTo(line.getPriceAtPick());
        assertThat(reloadedLine.getTierSnapshot()).as("line.tierSnapshot").isEqualTo(line.getTierSnapshot());
        assertThat(reloadedLine.getMenuItemNameSnapshot()).as("line.menuItemNameSnapshot").isEqualTo(line.getMenuItemNameSnapshot());
    }

    @Test
    @DisplayName("persist order and remove line, then persist again")
    void persistAndLoadOrders_line_manipulations_ok() {
        var belt = beltRepository.create(Belt.create("Default", 10, List.of(new SeatSpec("A1", 1))));
        var seatId = beltRepository.findById(belt.getId()).orElseThrow().getSeats().getFirst().getId();
        var plate = plateRepository.save(Plate.create(SALMON_NIGIRI_ID, PlateTier.RED, MoneyYen.of(400), inTwoHours()));

        var order = Order.open(seatId);
        order.addLineFromPlate(plate.getId(), "Salmon Nigiri", PlateTier.GREEN, 400);

        var savedOrder = repository.save(order);
        em.flush();
        em.clear();

        var firstReload = repository.findById(savedOrder.getId()).orElseThrow();
        assertThat(firstReload.getLines()).as("firstReload.lines").isNotNull().isNotEmpty();

        firstReload.removeLine(firstReload.getLines().getFirst());
        repository.save(firstReload);
        em.flush();
        em.clear();

        var secondReload = repository.findById(savedOrder.getId()).orElseThrow();
        assertThat(secondReload.getLines()).as("secondReload.lines").isNotNull().isEmpty();
    }

    @Test
    @DisplayName("save fails when seat is missing")
    void saveFailsWhenSeatMissing() {
        var missingSeatId = UUID.randomUUID();
        var order = Order.open(missingSeatId);

        repository.save(order);
        assertThrows(Exception.class, em::flush);
    }

    @Test
    @DisplayName("persist checks for null values")
    void persistAndLoadOrders_not_ok() {
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
        assertThrows(IllegalArgumentException.class, () -> repository.findById(null));
        assertThat(repository.findById(UUID.randomUUID())).as("findById(random)").isNotPresent();
    }
}