package com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper;

import com.tuempresa.ecommerce.orders.domain.model.ServiceLog;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.ServiceLogEntity;

public class ServiceLogMapper {

    private ServiceLogMapper() {
    }

    public static ServiceLogEntity toEntity(ServiceLog serviceLog) {
        if (serviceLog == null) {
            return null;
        }
        ServiceLogEntity entity = new ServiceLogEntity();
        entity.setId(serviceLog.getId());
        entity.setTimestamp(serviceLog.getTimestamp());
        entity.setServiceName(serviceLog.getServiceName());
        entity.setEndpoint(serviceLog.getEndpoint());
        entity.setHttpMethod(serviceLog.getHttpMethod());
        entity.setStatusCode(serviceLog.getStatusCode());
        entity.setMessage(serviceLog.getMessage());
        entity.setTraceId(serviceLog.getTraceId());
        return entity;
    }

    public static ServiceLog toDomain(ServiceLogEntity entity) {
        if (entity == null) {
            return null;
        }
        ServiceLog serviceLog = new ServiceLog();
        serviceLog.setId(entity.getId());
        serviceLog.setTimestamp(entity.getTimestamp());
        serviceLog.setServiceName(entity.getServiceName());
        serviceLog.setEndpoint(entity.getEndpoint());
        serviceLog.setHttpMethod(entity.getHttpMethod());
        serviceLog.setStatusCode(entity.getStatusCode());
        serviceLog.setMessage(entity.getMessage());
        serviceLog.setTraceId(entity.getTraceId());
        return serviceLog;
    }
}


