package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltSlotEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.PlateEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.BeltMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.BeltSlotMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.BeltJpaDao;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaBeltRepository implements BeltRepository {

    private final BeltJpaDao dao;
    private final BeltMapper mapper;
    private final BeltSlotMapper slotMapper;
    private final EntityManager em;

    public JpaBeltRepository(BeltJpaDao dao, BeltMapper mapper, BeltSlotMapper slotMapper, EntityManager em) {
        this.dao = dao;
        this.mapper = mapper;
        this.slotMapper = slotMapper;
        this.em = em;
    }

    @Override
    public Optional<Belt> findById(UUID id) {
        if (id == null) throw new IllegalArgumentException("Id cannot be null");
        return dao.findWithSlotsById(id).map(mapper::toDomain);
    }

    @Override
    public Belt save(Belt belt) {
        if (belt == null) throw new IllegalArgumentException("Belt cannot be null");

        var entity = dao.findWithSlotsById(belt.getId())
                .orElseGet(() -> new BeltEntity(
                        belt.getId(),
                        belt.getName(),
                        belt.getSlotCount(),
                        belt.getBaseRotationOffset(),
                        belt.getOffsetStartedAt(),
                        belt.getTickIntervalMs(),
                        belt.getSpeedSlotsPerTick()
                ));

        entity.setBaseRotationOffset(belt.getBaseRotationOffset());
        entity.setTickIntervalMs(belt.getTickIntervalMs());
        entity.setSpeedSlotsPerTick(belt.getSpeedSlotsPerTick());

        if (entity.getSlots() == null || entity.getSlots().isEmpty()) {
            var newSlots = belt.getSlots().stream()
                    .map(s -> {
                        PlateEntity plateRef = (s.getPlateId() == null) ? null : em.getReference(PlateEntity.class, s.getPlateId());
                        return slotMapper.toEntity(s, entity, plateRef);
                    })
                    .toList();

            entity.replaceSlots(newSlots);
        } else {
            // Update existing slots in place by positionIndex
            var byPos = entity.getSlots().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            BeltSlotEntity::getPositionIndex,
                            s -> s
                    ));

            for (var domainSlot : belt.getSlots()) {
                var slotEntity = byPos.get(domainSlot.getPositionIndex());
                if (slotEntity == null) {
                    throw new IllegalStateException("Missing slot entity for position " + domainSlot.getPositionIndex());
                }
                PlateEntity plateRef = (domainSlot.getPlateId() == null)
                        ? null
                        : em.getReference(PlateEntity.class, domainSlot.getPlateId());
                slotEntity.setPlate(plateRef);
            }
        }

        var saved = dao.save(entity);
        return mapper.toDomain(saved);
    }
}