package com.tuempresa.ecommerce.orders.infrastructure.persistence.adapter;

import com.tuempresa.ecommerce.orders.domain.model.ServiceLog;
import com.tuempresa.ecommerce.orders.domain.port.out.ServiceLogRepositoryPort;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.ServiceLogEntity;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper.ServiceLogMapper;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.repository.ServiceLogJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class ServiceLogRepositoryAdapter implements ServiceLogRepositoryPort {

    private final ServiceLogJpaRepository repository;

    public ServiceLogRepositoryAdapter(ServiceLogJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ServiceLog save(ServiceLog serviceLog) {
        ServiceLogEntity saved = repository.save(ServiceLogMapper.toEntity(serviceLog));
        return ServiceLogMapper.toDomain(saved);
    }
}


