package com.tuempresa.ecommerce.orders.infrastructure.persistence.repository;

import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.OrderSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderSettingsJpaRepository extends JpaRepository<OrderSettingsEntity, Long> {

    Optional<OrderSettingsEntity> findTopByOrderByIdAsc();
}


