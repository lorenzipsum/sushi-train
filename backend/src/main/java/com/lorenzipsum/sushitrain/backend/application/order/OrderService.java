package com.lorenzipsum.sushitrain.backend.application.order;

import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.application.common.PlateNotPickableException;
import com.lorenzipsum.sushitrain.backend.application.common.SeatAlreadyOccupiedException;
import com.lorenzipsum.sushitrain.backend.application.common.SeatNotOccupiedException;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItemRepository;
import com.lorenzipsum.sushitrain.backend.domain.order.Order;
import com.lorenzipsum.sushitrain.backend.domain.order.OrderRepository;
import com.lorenzipsum.sushitrain.backend.domain.plate.PlateRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.OrderJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.SeatJpaDao;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto.OrderLineDto;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto.OrderSummaryDto;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto.SeatOrderDto;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto.SeatOrderDtoMapper;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto.SeatStateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final SeatJpaDao seatRepository;
    private final PlateRepository plateRepository;
    private final MenuItemRepository menuItemRepository;
    private final SeatOrderDtoMapper mapper;
    private final OrderJpaDao orderJpaDao;

    public OrderService(OrderRepository orderRepository, SeatJpaDao seatRepository, PlateRepository plateRepository, MenuItemRepository menuItemRepository, SeatOrderDtoMapper mapper, OrderJpaDao orderJpaDao) {
        this.orderRepository = orderRepository;
        this.seatRepository = seatRepository;
        this.plateRepository = plateRepository;
        this.menuItemRepository = menuItemRepository;
        this.mapper = mapper;
        this.orderJpaDao = orderJpaDao;
    }

    @Transactional
    public SeatStateDto occupySeat(UUID seatId) {
        var seat = seatRepository.findById(seatId).orElseThrow(
                () -> new ResourceNotFoundException("Seat", seatId)
        );
        if (seatRepository.isSeatOccupied(seatId)) {
            throw new SeatAlreadyOccupiedException(seatId);
        }
        orderRepository.save(Order.open(seatId));
        return new SeatStateDto(seat.getId(), seat.getLabel(), seat.getPositionIndex(), seatRepository.isSeatOccupied(seatId));
    }

    @Transactional(readOnly = true)
    public SeatOrderDto getSeatState(UUID seatId) {
        var seat = seatRepository.findById(seatId).orElseThrow(
                () -> new ResourceNotFoundException("Seat", seatId)
        );

        Optional<Order> optional = orderRepository.findBySeatId(seatId);

        return new SeatOrderDto(
                seat.getId(),
                seat.getLabel(),
                seat.getPositionIndex(),
                seatRepository.isSeatOccupied(seatId),
                optional.map(mapper::toSeatOrderDto).orElse(null)
        );
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryDto> getAllOrders(Pageable pageable) {
        var headerPage = orderJpaDao.findOrderHeaders(pageable);
        var orderIds = headerPage.getContent().stream()
                .map(OrderJpaDao.OrderHeaderView::getOrderId)
                .toList();

        Map<UUID, java.util.List<OrderLineDto>> linesByOrderId = orderIds.isEmpty()
                ? java.util.Map.of()
                : orderJpaDao.findOrderLinesByOrderIds(orderIds).stream()
                .collect(Collectors.groupingBy(
                        OrderJpaDao.OrderLineView::getOrderId,
                        Collectors.mapping(
                                line -> new OrderLineDto(line.getMenuItemName(), line.getPlateTier(), line.getPrice()),
                                Collectors.toList()
                        )
                ));

        return headerPage.map(header -> {
            var lines = linesByOrderId.getOrDefault(header.getOrderId(), java.util.List.of());
            int totalPrice = lines.stream().mapToInt(OrderLineDto::price).sum();
            return new OrderSummaryDto(
                    header.getOrderId(),
                    header.getSeatId(),
                    header.getStatus(),
                    header.getCreatedAt(),
                    header.getClosedAt(),
                    lines,
                    totalPrice
            );
        });
    }

    @Transactional
    public SeatOrderDto pickPlate(UUID seatId, UUID plateId) {
        var seat = seatRepository.findById(seatId).orElseThrow(
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

        order.addLineFromPlate(
                plate.getId(),
                menuItem.getName(),
                plate.getTierSnapshot(),
                plate.getPriceAtCreation().amount()
        );

        Order savedOrder = orderRepository.save(order);

        return new SeatOrderDto(
                seat.getId(),
                seat.getLabel(),
                seat.getPositionIndex(),
                true,
                mapper.toSeatOrderDto(savedOrder)
        );
    }

    @Transactional
    public SeatOrderDto checkout(UUID seatId) {
        var seat = seatRepository.findById(seatId).orElseThrow(
                () -> new ResourceNotFoundException("Seat", seatId)
        );

        Order order = orderRepository.findBySeatIdForUpdate(seatId).orElseThrow(
                () -> new SeatNotOccupiedException(seatId)
        );

        order.checkout();
        Order savedOrder = orderRepository.save(order);

        return new SeatOrderDto(
                seat.getId(),
                seat.getLabel(),
                seat.getPositionIndex(),
                seatRepository.isSeatOccupied(seatId),
                mapper.toSeatOrderDto(savedOrder)
        );
    }
}
