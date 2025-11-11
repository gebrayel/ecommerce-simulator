package com.tuempresa.ecommerce.orders.infrastructure.persistence.repository;

import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartJpaRepository extends JpaRepository<CartEntity, Long> {
}


