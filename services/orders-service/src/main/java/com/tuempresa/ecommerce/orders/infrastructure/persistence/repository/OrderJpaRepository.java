package com.tuempresa.ecommerce.orders.infrastructure.persistence.repository;

import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
}


