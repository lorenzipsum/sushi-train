package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.domain.plate.PlateRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.MenuItemEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.PlateEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.PlateMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.MenuItemJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.PlateJpaDao;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaPlateRepository implements PlateRepository {

    private final PlateJpaDao dao;
    private final EntityManager em;
    private final PlateMapper mapper;
    private final MenuItemJpaDao menuItemJpaDao;

    public JpaPlateRepository(PlateJpaDao dao, EntityManager em, PlateMapper mapper, MenuItemJpaDao menuItemJpaDao) {
        this.dao = dao;
        this.em = em;
        this.mapper = mapper;
        this.menuItemJpaDao = menuItemJpaDao;
    }

    @Override
    public Optional<Plate> findById(UUID id) {
        if (id == null) throw new IllegalArgumentException("Id cannot be null");
        return dao.findById(id).map(mapper::toDomain);
    }

    @Override
    public Plate save(Plate plate) {
        if (plate == null) throw new IllegalArgumentException("Plate cannot be null");

        PlateEntity existingPlate = em.find(PlateEntity.class, plate.getId());

        if (existingPlate != null) {
            existingPlate.setStatus(plate.getStatus());
            return mapper.toDomain(existingPlate);
        }

        MenuItemEntity menuItem = menuItemJpaDao.getReferenceById(plate.getMenuItemId());
        PlateEntity newPlate = mapper.toEntity(plate, menuItem);
        em.persist(newPlate);

        return mapper.toDomain(newPlate);
    }

    @Override
    public Page<Plate> findAll(Pageable pageable) {
        return dao.findAll(pageable).map(mapper::toDomain);
    }

}