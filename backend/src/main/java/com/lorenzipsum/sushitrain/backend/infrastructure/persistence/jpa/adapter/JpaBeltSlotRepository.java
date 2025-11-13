package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.domain.belt.BeltSlot;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltSlotRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.PlateEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.BeltSlotMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.BeltJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.BeltSlotJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.PlateJpaDao;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaBeltSlotRepository implements BeltSlotRepository {

    private final BeltSlotJpaDao dao;
    private final BeltSlotMapper mapper;
    private final BeltJpaDao beltDao;
    private final PlateJpaDao plateDao;

    public JpaBeltSlotRepository(BeltSlotJpaDao dao, BeltSlotMapper mapper, BeltJpaDao beltDao, PlateJpaDao plateDao) {
        this.dao = dao;
        this.mapper = mapper;
        this.beltDao = beltDao;
        this.plateDao = plateDao;
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

        PlateEntity plateEntity = null;
        if (beltSlot.getPlateId() != null)
            plateEntity = plateDao.findById(beltSlot.getPlateId()).orElseThrow(() -> new IllegalStateException("Plate not found for id: " + beltSlot.getPlateId()));

        var saved = dao.save(mapper.toEntity(beltSlot, beltEntity, plateEntity));
        return mapper.toDomain(saved);
    }
}