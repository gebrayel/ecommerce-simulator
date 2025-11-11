package com.tuempresa.ecommerce.orders.infrastructure.persistence.adapter;

import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderRepositoryPort;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.OrderEntity;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper.OrderMapper;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.repository.OrderJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository orderJpaRepository;

    public OrderRepositoryAdapter(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = OrderMapper.toEntity(order);
        OrderEntity saved = orderJpaRepository.save(entity);
        return OrderMapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderJpaRepository.findById(id)
                .map(OrderMapper::toDomain);
    }
}


