package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo;

import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SeatJpaDao extends JpaRepository<SeatEntity, UUID> {
}
