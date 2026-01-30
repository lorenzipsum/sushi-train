package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.domain.order.Order;
import com.lorenzipsum.sushitrain.backend.domain.order.OrderRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.OrderEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.OrderLineEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.PlateEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.SeatEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.OrderLineMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.OrderMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.OrderJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.PlateJpaDao;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaOrderRepository implements OrderRepository {

    private final OrderJpaDao dao;
    private final OrderMapper mapper;
    private final OrderLineMapper orderLineMapper;
    private final PlateJpaDao plateJpaDao;
    private final EntityManager em;

    public JpaOrderRepository(OrderJpaDao dao, OrderMapper mapper, OrderLineMapper orderLineMapper, PlateJpaDao plateJpaDao, EntityManager em) {
        this.dao = dao;
        this.mapper = mapper;
        this.orderLineMapper = orderLineMapper;
        this.plateJpaDao = plateJpaDao;
        this.em = em;
    }

    @Override
    public Optional<Order> findById(UUID id) {
        if (id == null) throw new IllegalArgumentException("Id cannot be null");
        return dao.findById(id).map(mapper::toDomain);
    }

    @Override
    public Order save(Order order) {
        if (order == null) throw new IllegalArgumentException("Order cannot be null");

        SeatEntity seatRef = em.getReference(SeatEntity.class, order.getSeatId());
        OrderEntity orderEntity = mapper.toEntity(order, seatRef);

        for (var line : order.getLines()) {
            PlateEntity plateRef = plateJpaDao.getReferenceById(line.getPlateId());
            OrderLineEntity lineEntity = orderLineMapper.toEntity(line, plateRef, orderEntity);
            orderEntity.addLine(lineEntity);
        }

        OrderEntity saved = dao.save(orderEntity);
        return mapper.toDomain(saved);
    }
}