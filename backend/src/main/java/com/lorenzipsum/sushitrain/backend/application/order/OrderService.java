package com.lorenzipsum.sushitrain.backend.application.order;

import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.application.common.SeatAlreadyOccupiedException;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItemRepository;
import com.lorenzipsum.sushitrain.backend.domain.order.Order;
import com.lorenzipsum.sushitrain.backend.domain.order.OrderRepository;
import com.lorenzipsum.sushitrain.backend.domain.plate.PlateRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.SeatJpaDao;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto.SeatOrderDto;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto.SeatOrderDtoMapper;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto.SeatStateDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final SeatJpaDao seatRepository;
    private final PlateRepository plateRepository;
    private final MenuItemRepository menuItemRepository;
    private final SeatOrderDtoMapper mapper;

    public OrderService(OrderRepository orderRepository, SeatJpaDao seatRepository, PlateRepository plateRepository, MenuItemRepository menuItemRepository, SeatOrderDtoMapper mapper) {
        this.orderRepository = orderRepository;
        this.seatRepository = seatRepository;
        this.plateRepository = plateRepository;
        this.menuItemRepository = menuItemRepository;
        this.mapper = mapper;
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

        Order order = orderRepository.findBySeatId(seatId).orElseThrow(
                () -> new IllegalStateException("No open order found for occupied seat: " + seatId)
        );

        return new SeatOrderDto(
                seat.getId(),
                seat.getLabel(),
                seat.getPositionIndex(),
                seatRepository.isSeatOccupied(seatId),
                mapper.toSeatOrderDto(order)
        );
    }

    @Transactional
    public SeatOrderDto pickPlate(UUID seatId, UUID plateId) {
        var seat = seatRepository.findById(seatId).orElseThrow(
                () -> new ResourceNotFoundException("Seat", seatId)
        );
        var plate = plateRepository.findById(plateId).orElseThrow(
                () -> new ResourceNotFoundException("Plate", plateId)
        );
        var menuItem = menuItemRepository.findById(plate.getMenuItemId()).orElseThrow(
                () -> new ResourceNotFoundException("MenuItem", plate.getMenuItemId())
        );

        Order order = orderRepository.findBySeatId(seatId).orElseThrow(
                () -> new IllegalStateException("No open order found for occupied seat: " + seatId)
        );

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
}
