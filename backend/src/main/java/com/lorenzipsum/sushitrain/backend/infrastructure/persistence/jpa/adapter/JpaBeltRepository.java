package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltSlotEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.SeatEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.BeltMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.BeltSlotMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.SeatMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.BeltJpaDao;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaBeltRepository implements BeltRepository {

    private final BeltJpaDao dao;
    private final EntityManager em;
    private final BeltMapper mapper;
    private final BeltSlotMapper slotMapper;
    private final SeatMapper seatMapper;

    public JpaBeltRepository(EntityManager em, BeltJpaDao dao, BeltMapper mapper, BeltSlotMapper slotMapper, SeatMapper seatMapper) {
        this.em = em;
        this.dao = dao;
        this.mapper = mapper;
        this.slotMapper = slotMapper;
        this.seatMapper = seatMapper;
    }

    @Override
    public List<Belt> findAllBelts() {
        return dao.findAllBeltsWithParams().stream().map(
                p -> Belt.rehydrate(
                        p.getId(),
                        p.getName(),
                        p.getSlotCount(),
                        p.getBaseRotationOffset(),
                        p.getTickIntervalMs(),
                        p.getSpeedSlotsPerTick(),
                        List.of(),
                        List.of(),
                        p.getOffsetStartedAt()
                )).toList();
    }

    @Override
    public Optional<Belt> findById(UUID id) {
        if (id == null) throw new IllegalArgumentException("Id cannot be null");
        return dao.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Belt> findParamsById(UUID id) {
        return dao.findParamsById(id).map(p ->
                Belt.rehydrate(
                        p.getId(),
                        p.getName(),
                        p.getSlotCount(),
                        p.getBaseRotationOffset(),
                        p.getTickIntervalMs(),
                        p.getSpeedSlotsPerTick(),
                        List.of(),
                        List.of(),
                        p.getOffsetStartedAt()
                )
        );
    }

    @Override
    public Belt saveParams(Belt belt) {
        int updated = dao.updateParams(
                belt.getId(),
                belt.getTickIntervalMs(),
                belt.getSpeedSlotsPerTick(),
                belt.getBaseRotationOffset(),
                belt.getOffsetStartedAt()
        );
        if (updated == 0) throw new ResourceNotFoundException("Belt", belt.getId());
        return belt;
    }

    @Override
    public Belt create(Belt belt) {
        if (belt == null) throw new IllegalArgumentException("Belt cannot be null");

        BeltEntity newBelt = mapper.toEntity(belt);

        List<BeltSlotEntity> beltSlots = belt.getSlots().stream()
                .map(slot -> slotMapper.toEntity(slot, newBelt, null))
                .toList();
        newBelt.replaceSlots(beltSlots);

        List<SeatEntity> seats = belt.getSeats().stream()
                .map(seat -> seatMapper.toEntity(seat, newBelt))
                .toList();
        newBelt.replaceSeats(seats);

        em.persist(newBelt);
        return belt;
    }
}