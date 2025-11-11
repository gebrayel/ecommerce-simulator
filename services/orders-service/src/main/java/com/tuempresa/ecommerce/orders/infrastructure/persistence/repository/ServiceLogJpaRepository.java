package com.tuempresa.ecommerce.orders.infrastructure.persistence.repository;

import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.ServiceLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServiceLogJpaRepository extends JpaRepository<ServiceLogEntity, UUID> {
}


