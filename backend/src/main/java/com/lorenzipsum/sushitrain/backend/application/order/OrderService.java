package com.lorenzipsum.sushitrain.backend.application.order;

import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.application.common.SeatAlreadyOccupiedException;
import com.lorenzipsum.sushitrain.backend.domain.order.Order;
import com.lorenzipsum.sushitrain.backend.domain.order.OrderRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.SeatJpaDao;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto.SeatStateDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final SeatJpaDao seatRepository;

    public OrderService(OrderRepository orderRepository, SeatJpaDao seatRepository) {
        this.orderRepository = orderRepository;
        this.seatRepository = seatRepository;
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
    public SeatStateDto getSeatState(UUID seatId) {
        var seat = seatRepository.findById(seatId).orElseThrow(
                () -> new ResourceNotFoundException("Seat", seatId)
        );
        return new SeatStateDto(seat.getId(), seat.getLabel(), seat.getPositionIndex(), seatRepository.isSeatOccupied(seatId));
    }
}
