package com.lorenzipsum.sushitrain.backend.application.order;

import com.lorenzipsum.sushitrain.backend.application.view.SeatStateView;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.common.YenAmount;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItem;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItemRepository;
import com.lorenzipsum.sushitrain.backend.domain.order.Order;
import com.lorenzipsum.sushitrain.backend.domain.order.OrderRepository;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.domain.plate.PlateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private SeatQueryPort seatQueryPort;
    @Mock
    private PlateRepository plateRepository;
    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private OrderQueryPort orderQueryPort;
    @Mock
    private BeltSlotCommandPort beltSlotCommandPort;

    @Test
    void occupySeat_usesSeatLockAndReturnsOccupiedTrue() {
        UUID seatId = UUID.randomUUID();
        var beltId = UUID.randomUUID();
        var seatInfo = new SeatQueryPort.SeatInfo(seatId, beltId, "A1", 0);

        when(seatQueryPort.findSeatByIdForUpdate(seatId)).thenReturn(Optional.of(seatInfo));
        when(seatQueryPort.isSeatOccupied(seatId)).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var service = new OrderService(
                orderRepository,
                seatQueryPort,
                plateRepository,
                menuItemRepository,
                orderQueryPort,
                beltSlotCommandPort
        );

        SeatStateView state = service.occupySeat(seatId);

        assertThat(state.seatId()).isEqualTo(seatId);
        assertThat(state.isOccupied()).isTrue();
        verify(seatQueryPort).findSeatByIdForUpdate(seatId);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void pickPlate_clearsBeltSlotAssignmentInSameFlow() {
        UUID seatId = UUID.randomUUID();
        UUID plateId = UUID.randomUUID();
        UUID menuItemId = UUID.randomUUID();
        UUID beltId = UUID.randomUUID();

        var seatInfo = new SeatQueryPort.SeatInfo(seatId, beltId, "A1", 0);
        var plate = Plate.rehydrate(
                plateId,
                menuItemId,
                PlateTier.GREEN,
                YenAmount.of(450),
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T02:00:00Z"),
                PlateStatus.ON_BELT
        );
        var menuItem = MenuItem.rehydrate(
                menuItemId,
                "Salmon Nigiri",
                PlateTier.GREEN,
                YenAmount.of(450),
                Instant.parse("2026-01-01T00:00:00Z")
        );
        var openOrder = Order.open(seatId);

        when(seatQueryPort.findSeatById(seatId)).thenReturn(Optional.of(seatInfo));
        when(plateRepository.findByIdForUpdate(plateId)).thenReturn(Optional.of(plate));
        when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(menuItem));
        when(orderRepository.findBySeatIdForUpdate(seatId)).thenReturn(Optional.of(openOrder));
        when(plateRepository.save(any(Plate.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var service = new OrderService(
                orderRepository,
                seatQueryPort,
                plateRepository,
                menuItemRepository,
                orderQueryPort,
                beltSlotCommandPort
        );

        service.pickPlate(seatId, plateId);

        InOrder inOrder = inOrder(plateRepository, beltSlotCommandPort, orderRepository);
        inOrder.verify(plateRepository).save(any(Plate.class));
        inOrder.verify(beltSlotCommandPort).clearPlateAssignment(plateId);
        inOrder.verify(orderRepository).save(any(Order.class));
    }
}
