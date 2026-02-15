package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

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
    public Optional<Belt> findById(UUID id) {
        if (id == null) throw new IllegalArgumentException("Id cannot be null");
        return dao.findById(id).map(mapper::toDomain);
    }

    @Override
    public Belt save(Belt belt) {
        // only belt params can be updated, not seats or slots (plate to slot assignment is not handled here)

        if (belt == null) throw new IllegalArgumentException("Belt cannot be null");

        BeltEntity existingBelt = em.find(BeltEntity.class, belt.getId());

        if (existingBelt != null) {
            existingBelt.setSpeedSlotsPerTick(belt.getSpeedSlotsPerTick());
            existingBelt.setTickIntervalMs(belt.getTickIntervalMs());
            existingBelt.setBaseRotationOffset(belt.getBaseRotationOffset());
            existingBelt.setOffsetStartedAt(belt.getOffsetStartedAt());
            return mapper.toDomain(existingBelt);

        }

        BeltEntity newBelt = mapper.toEntity(belt);
        // new belt is created without assigned plates
        List<BeltSlotEntity> beltSlots = belt.getSlots().stream().map(slot -> slotMapper.toEntity(slot, newBelt, null)).toList();
        newBelt.replaceSlots(beltSlots);
        List<SeatEntity> seats = belt.getSeats().stream().map(seat -> seatMapper.toEntity(seat, newBelt)).toList();
        newBelt.replaceSeats(seats);
        em.persist(newBelt);
        return mapper.toDomain(newBelt);
    }
}