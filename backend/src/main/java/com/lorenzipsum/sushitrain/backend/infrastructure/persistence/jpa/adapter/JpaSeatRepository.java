package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.domain.seat.Seat;
import com.lorenzipsum.sushitrain.backend.domain.seat.SeatRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.SeatMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.BeltJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.SeatJpaDao;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaSeatRepository implements SeatRepository {

    private final SeatJpaDao dao;
    private final SeatMapper mapper;
    private final BeltJpaDao beltDao;

    public JpaSeatRepository(SeatJpaDao dao, SeatMapper mapper, BeltJpaDao beltDao) {
        this.dao = dao;
        this.mapper = mapper;
        this.beltDao = beltDao;
    }

    @Override
    public Optional<Seat> findById(UUID id) {
        if (id == null) throw new IllegalArgumentException("Id cannot be null");
        return dao.findById(id).map(mapper::toDomain);
    }

    @Override
    public Seat save(Seat seat) {
        if (seat == null) throw new IllegalArgumentException("Seat cannot be null");
        BeltEntity beltEntity = beltDao.getReferenceById(seat.getBeltId());
        var saved = dao.save(mapper.toEntity(seat, beltEntity));
        return mapper.toDomain(saved);
    }
}