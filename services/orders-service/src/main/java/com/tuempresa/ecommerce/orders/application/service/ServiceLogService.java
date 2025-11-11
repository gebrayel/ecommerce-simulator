package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.model.ServiceLog;
import com.tuempresa.ecommerce.orders.domain.port.in.ServiceLogUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.ServiceLogRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class ServiceLogService implements ServiceLogUseCase {

    private final ServiceLogRepositoryPort repositoryPort;
    private final String serviceName;

    public ServiceLogService(ServiceLogRepositoryPort repositoryPort,
                             @Value("${spring.application.name:orders-service}") String serviceName) {
        this.repositoryPort = repositoryPort;
        this.serviceName = serviceName;
    }

    @Override
    public void log(String traceId, String endpoint, String httpMethod, Integer statusCode, String message) {
        ServiceLog serviceLog = new ServiceLog(
                UUID.randomUUID(),
                LocalDateTime.now(),
                serviceName,
                endpoint,
                httpMethod,
                statusCode,
                message,
                traceId
        );
        repositoryPort.save(serviceLog);
    }
}


