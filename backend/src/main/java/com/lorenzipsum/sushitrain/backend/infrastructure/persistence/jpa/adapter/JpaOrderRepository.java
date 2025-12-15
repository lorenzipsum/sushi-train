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
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.SeatJpaDao;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaOrderRepository implements OrderRepository {

    private final OrderJpaDao dao;
    private final OrderMapper mapper;
    private final OrderLineMapper orderLineMapper;
    private final SeatJpaDao seatDao;
    private final PlateJpaDao plateJpaDao;

    public JpaOrderRepository(OrderJpaDao dao, OrderMapper mapper, OrderLineMapper orderLineMapper, SeatJpaDao seatDao, PlateJpaDao plateJpaDao) {
        this.dao = dao;
        this.mapper = mapper;
        this.orderLineMapper = orderLineMapper;
        this.seatDao = seatDao;
        this.plateJpaDao = plateJpaDao;
    }

    @Override
    public Optional<Order> findById(UUID id) {
        if (id == null) throw new IllegalArgumentException("Id cannot be null");
        return dao.findById(id).map(mapper::toDomain);
    }

    @Override
    public Order save(Order order) {
        if (order == null) throw new IllegalArgumentException("Order cannot be null");
        SeatEntity seatEntity = seatDao.getReferenceById(order.getSeatId());

        OrderEntity orderEntity = mapper.toEntity(order, seatEntity, new ArrayList<>());

        List<OrderLineEntity> orderLineEntities = order.getLines().stream().map((line) ->
                {
                    PlateEntity plateEntity = plateJpaDao.getReferenceById(line.getPlateId());
                    OrderLineEntity orderLineEntity = orderLineMapper.toEntity(line, plateEntity, orderEntity);
                    orderEntity.addLine(orderLineEntity);
                    return orderLineEntity;
                }
        ).toList();
        var saved = dao.save(mapper.toEntity(order, seatEntity, orderLineEntities));
        return mapper.toDomain(saved);
    }
}