package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo;

import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderJpaDao extends JpaRepository<OrderEntity, UUID> {
}
