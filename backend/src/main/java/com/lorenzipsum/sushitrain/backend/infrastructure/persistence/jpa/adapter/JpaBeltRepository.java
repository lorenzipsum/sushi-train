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

        BeltEntity managed = em.find(BeltEntity.class, belt.getId());

        if (managed != null) {
            managed.setSpeedSlotsPerTick(belt.getSpeedSlotsPerTick());
            managed.setTickIntervalMs(belt.getTickIntervalMs());
            managed.setBaseRotationOffset(belt.getBaseRotationOffset());
            managed.setOffsetStartedAt(belt.getOffsetStartedAt());
            return mapper.toDomain(managed);

        } else {
            BeltEntity created = mapper.toEntity(belt);
            // new belt is created without assigned plates
            List<BeltSlotEntity> beltSlots = belt.getSlots().stream().map(slot -> slotMapper.toEntity(slot, created, null)).toList();
            created.replaceSlots(beltSlots);
            List<SeatEntity> seats = belt.getSeats().stream().map(seat -> seatMapper.toEntity(seat, created)).toList();
            created.replaceSeats(seats);
            em.persist(created);
            return mapper.toDomain(created);
        }
    }
}