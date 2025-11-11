package com.tuempresa.ecommerce.orders.infrastructure.persistence.adapter;

import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderSettingsRepositoryPort;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.OrderSettingsEntity;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper.OrderSettingsMapper;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.repository.OrderSettingsJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderSettingsRepositoryAdapter implements OrderSettingsRepositoryPort {

    private final OrderSettingsJpaRepository repository;

    public OrderSettingsRepositoryAdapter(OrderSettingsJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<OrderSettings> findSettings() {
        return repository.findTopByOrderByIdAsc()
                .map(OrderSettingsMapper::toDomain);
    }

    @Override
    public OrderSettings save(OrderSettings settings) {
        OrderSettingsEntity saved = repository.save(OrderSettingsMapper.toEntity(settings));
        return OrderSettingsMapper.toDomain(saved);
    }
}


