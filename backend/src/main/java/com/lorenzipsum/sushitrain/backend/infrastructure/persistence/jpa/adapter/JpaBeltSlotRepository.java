package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.domain.belt.BeltSlot;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltSlotRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.BeltSlotMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.BeltJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.BeltSlotJpaDao;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaBeltSlotRepository implements BeltSlotRepository {

    private final BeltSlotJpaDao dao;
    private final BeltSlotMapper mapper;
    private final BeltJpaDao beltDao;

    public JpaBeltSlotRepository(BeltSlotJpaDao dao, BeltSlotMapper mapper, BeltJpaDao beltDao) {
        this.dao = dao;
        this.mapper = mapper;
        this.beltDao = beltDao;
    }

    @Override
    public Optional<BeltSlot> findById(UUID id) {
        if (id == null) throw new IllegalArgumentException("Id cannot be null");
        return dao.findById(id).map(mapper::toDomain);
    }

    @Override
    public BeltSlot save(BeltSlot beltSlot) {
        if (beltSlot == null) throw new IllegalArgumentException("BeltSlot cannot be null");

        BeltEntity beltEntity = beltDao.findById(beltSlot.getBeltId()).orElseThrow(
                () -> new IllegalStateException("Belt not found for id: " + beltSlot.getBeltId()));

        var saved = dao.save(mapper.toEntity(beltSlot, beltEntity));
        return mapper.toDomain(saved);
    }
}