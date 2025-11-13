package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo;

import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.PlateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlateJpaDao extends JpaRepository<PlateEntity, UUID> {
}
