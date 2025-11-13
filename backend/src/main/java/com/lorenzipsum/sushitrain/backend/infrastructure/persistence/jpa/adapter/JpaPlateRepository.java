package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.domain.plate.PlateRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.MenuItemEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.PlateMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.MenuItemJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.PlateJpaDao;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaPlateRepository implements PlateRepository {

    private final PlateJpaDao dao;
    private final PlateMapper mapper;
    private final MenuItemJpaDao menuItemJpaDao;

    public JpaPlateRepository(PlateJpaDao dao, PlateMapper mapper, MenuItemJpaDao menuItemJpaDao) {
        this.dao = dao;
        this.mapper = mapper;
        this.menuItemJpaDao = menuItemJpaDao;
    }

    @Override
    public Optional<Plate> findById(UUID uuid) {
        return dao.findById(uuid).map(mapper::toDomain);
    }

    @Override
    public Plate save(Plate plate) {
        if (plate == null) throw new IllegalArgumentException("Plate cannot be null");
        if (plate.getMenuItemId() == null) throw new IllegalArgumentException("Plate.menuItemId must not be null");

        MenuItemEntity menuItem = menuItemJpaDao.findById(plate.getMenuItemId()).orElseThrow(
                () -> new IllegalStateException("MenuItem not found for id: " + plate.getMenuItemId()));

        var saved = dao.save(mapper.toEntity(plate, menuItem));
        return mapper.toDomain(saved);
    }
}