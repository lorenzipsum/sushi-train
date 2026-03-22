package com.lorenzipsum.sushitrain.backend.application.order;

import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.application.common.PlateNotPickableException;
import com.lorenzipsum.sushitrain.backend.application.common.SeatAlreadyOccupiedException;
import com.lorenzipsum.sushitrain.backend.application.common.SeatNotOccupiedException;
import com.lorenzipsum.sushitrain.backend.application.view.OrderLineView;
import com.lorenzipsum.sushitrain.backend.application.view.OrderSummaryView;
import com.lorenzipsum.sushitrain.backend.application.view.SeatOrderView;
import com.lorenzipsum.sushitrain.backend.application.view.SeatStateView;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItemRepository;
import com.lorenzipsum.sushitrain.backend.domain.order.Order;
import com.lorenzipsum.sushitrain.backend.domain.order.OrderRepository;
import com.lorenzipsum.sushitrain.backend.domain.plate.PlateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final SeatQueryPort seatQueryPort;
    private final PlateRepository plateRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderQueryPort orderQueryPort;
    private final BeltSlotCommandPort beltSlotCommandPort;

    public OrderService(OrderRepository orderRepository,
                        SeatQueryPort seatQueryPort,
                        PlateRepository plateRepository,
                        MenuItemRepository menuItemRepository,
                        OrderQueryPort orderQueryPort,
                        BeltSlotCommandPort beltSlotCommandPort) {
        this.orderRepository = orderRepository;
        this.seatQueryPort = seatQueryPort;
        this.plateRepository = plateRepository;
        this.menuItemRepository = menuItemRepository;
        this.orderQueryPort = orderQueryPort;
        this.beltSlotCommandPort = beltSlotCommandPort;
    }

    @Transactional
    public SeatStateView occupySeat(UUID seatId) {
        var seat = seatQueryPort.findSeatByIdForUpdate(seatId).orElseThrow(
                () -> new ResourceNotFoundException("Seat", seatId)
        );
        if (seatQueryPort.isSeatOccupied(seatId)) {
            throw new SeatAlreadyOccupiedException(seatId);
        }
        orderRepository.save(Order.open(seatId));
        return new SeatStateView(seat.seatId(), seat.label(), seat.positionIndex(), true);
    }

    @Transactional(readOnly = true)
    public SeatOrderView getSeatState(UUID seatId) {
        var seat = seatQueryPort.findSeatById(seatId).orElseThrow(
                () -> new ResourceNotFoundException("Seat", seatId)
        );

        Optional<Order> optional = orderRepository.findBySeatId(seatId);

        return new SeatOrderView(
                seat.seatId(),
                seat.label(),
                seat.positionIndex(),
                seatQueryPort.isSeatOccupied(seatId),
                optional.map(this::toOrderSummaryView).orElse(null)
        );
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryView> getAllOrders(Pageable pageable) {
        return orderQueryPort.findOrderSummaries(pageable);
    }

    @Transactional
    public SeatOrderView pickPlate(UUID seatId, UUID plateId) {
        var seat = seatQueryPort.findSeatById(seatId).orElseThrow(
                () -> new ResourceNotFoundException("Seat", seatId)
        );
        var plate = plateRepository.findByIdForUpdate(plateId).orElseThrow(
                () -> new ResourceNotFoundException("Plate", plateId)
        );
        var menuItem = menuItemRepository.findById(plate.getMenuItemId()).orElseThrow(
                () -> new ResourceNotFoundException("MenuItem", plate.getMenuItemId())
        );
        var order = orderRepository.findBySeatIdForUpdate(seatId).orElseThrow(
                () -> new SeatNotOccupiedException(seatId)
        );
        try {
            plate.pick();
        } catch (IllegalStateException ex) {
            throw new PlateNotPickableException(plateId);
        }
        plateRepository.save(plate);
        beltSlotCommandPort.clearPlateAssignment(plate.getId());

        order.addLineFromPlate(
                plate.getId(),
                menuItem.getName(),
                plate.getTierSnapshot(),
                plate.getPriceAtCreation().amount()
        );

        Order savedOrder = orderRepository.save(order);

        return new SeatOrderView(
                seat.seatId(),
                seat.label(),
                seat.positionIndex(),
                true,
                toOrderSummaryView(savedOrder)
        );
    }

    @Transactional
    public SeatOrderView checkout(UUID seatId) {
        var seat = seatQueryPort.findSeatById(seatId).orElseThrow(
                () -> new ResourceNotFoundException("Seat", seatId)
        );

        Order order = orderRepository.findBySeatIdForUpdate(seatId).orElseThrow(
                () -> new SeatNotOccupiedException(seatId)
        );

        order.checkout();
        Order savedOrder = orderRepository.save(order);

        return new SeatOrderView(
                seat.seatId(),
                seat.label(),
                seat.positionIndex(),
                seatQueryPort.isSeatOccupied(seatId),
                toOrderSummaryView(savedOrder)
        );
    }

    @Transactional(readOnly = true)
    public UUID getBeltIdForSeat(UUID seatId) {
        return seatQueryPort.findSeatById(seatId)
                .map(SeatQueryPort.SeatInfo::beltId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", seatId));
    }

    private OrderSummaryView toOrderSummaryView(Order order) {
        var lines = order.getLines().stream()
                .map(line -> new OrderLineView(
                        line.getMenuItemNameSnapshot(),
                        line.getTierSnapshot(),
                        line.getPriceAtPick().amount()
                ))
                .toList();

        return new OrderSummaryView(
                order.getId(),
                order.getSeatId(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getClosedAt(),
                lines,
                order.total().amount()
        );
    }
}
