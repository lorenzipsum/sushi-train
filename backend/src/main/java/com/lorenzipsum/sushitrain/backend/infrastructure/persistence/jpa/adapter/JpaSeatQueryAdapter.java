package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.application.order.SeatQueryPort;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.SeatJpaDao;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class JpaSeatQueryAdapter implements SeatQueryPort {
    private final SeatJpaDao seatJpaDao;

    public JpaSeatQueryAdapter(SeatJpaDao seatJpaDao) {
        this.seatJpaDao = seatJpaDao;
    }

    @Override
    public Optional<SeatInfo> findSeatById(UUID seatId) {
        return seatJpaDao.findById(seatId)
                .map(seat -> new SeatInfo(seat.getId(), seat.getLabel(), seat.getPositionIndex()));
    }

    @Override
    public Optional<SeatInfo> findSeatByIdForUpdate(UUID seatId) {
        return seatJpaDao.findByIdForUpdate(seatId)
                .map(seat -> new SeatInfo(seat.getId(), seat.getLabel(), seat.getPositionIndex()));
    }

    @Override
    public boolean isSeatOccupied(UUID seatId) {
        return seatJpaDao.isSeatOccupied(seatId);
    }
}
