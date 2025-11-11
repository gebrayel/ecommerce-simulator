package com.tuempresa.ecommerce.orders.infrastructure.persistence.repository;

import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.RequestLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RequestLogRepository extends JpaRepository<RequestLogEntity, UUID> {
}


