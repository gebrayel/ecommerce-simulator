package com.tuempresa.ecommerce.orders.infrastructure.persistence.repository;

import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {
}


