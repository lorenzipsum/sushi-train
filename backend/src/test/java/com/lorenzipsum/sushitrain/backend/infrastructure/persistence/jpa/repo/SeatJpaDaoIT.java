package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo;

import com.lorenzipsum.sushitrain.backend.domain.belt.BeltRepository;
import com.lorenzipsum.sushitrain.backend.domain.order.Order;
import com.lorenzipsum.sushitrain.backend.domain.order.OrderRepository;
import com.lorenzipsum.sushitrain.backend.domain.plate.PlateRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter.JpaBaseRepositoryIT;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter.JpaBeltRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter.JpaOrderRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter.JpaPlateRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import static com.lorenzipsum.sushitrain.backend.testutil.TestFixtures.MAIN_BELT_ID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Import({
        JpaOrderRepository.class,
        JpaBeltRepository.class,
        JpaPlateRepository.class,
        OrderMapper.class,
        OrderLineMapper.class,
        BeltMapper.class,
        BeltSlotMapper.class,
        SeatMapper.class,
        PlateMapper.class})
public class SeatJpaDaoIT extends JpaBaseRepositoryIT {
    @Autowired
    private BeltRepository beltRepo;
    @Autowired
    private SeatJpaDao seatRepo;
    @Autowired
    private OrderRepository orderRepo;
    @Autowired
    private PlateRepository plateRepo;

    @Test
    void testIsSeatOccupied() {
        var belt = beltRepo.findById(MAIN_BELT_ID).orElseThrow();
        var seat = belt.getSeats().getFirst();
        var occupied = seatRepo.isSeatOccupied(seat.getId());
        assertThat(occupied).isFalse();
    }

    @Test
    void testIsSeatOccupied_nonExistingSeat() {
        var nonExistingSeatId = java.util.UUID.randomUUID();
        var occupied = seatRepo.isSeatOccupied(nonExistingSeatId);
        assertThat(occupied).isFalse();
    }

    @Test
    void testIsSeatOccupied_withOpenOrder() {
        var belt = beltRepo.findById(MAIN_BELT_ID).orElseThrow();
        var seat = belt.getSeats().getFirst();
        orderRepo.save(Order.open(seat.getId()));
        var occupied = seatRepo.isSeatOccupied(seat.getId());
        assertThat(occupied).isTrue();
    }

    @Test
    void testIsSeatOccupied_withClosedOrder() {
        var belt = beltRepo.findById(MAIN_BELT_ID).orElseThrow();
        var seat = belt.getSeats().getFirst();
        var order = orderRepo.save(Order.open(seat.getId()));
        order.checkout();
        orderRepo.save(order);
        var occupied = seatRepo.isSeatOccupied(seat.getId());
        assertThat(occupied).isFalse();
    }
}
