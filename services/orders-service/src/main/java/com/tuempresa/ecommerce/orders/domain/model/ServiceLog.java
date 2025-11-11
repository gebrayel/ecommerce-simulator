package com.tuempresa.ecommerce.orders.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ServiceLog {

    private UUID id;
    private LocalDateTime timestamp;
    private String serviceName;
    private String endpoint;
    private String httpMethod;
    private Integer statusCode;
    private String message;
    private String traceId;

    public ServiceLog() {
    }

    public ServiceLog(UUID id,
                      LocalDateTime timestamp,
                      String serviceName,
                      String endpoint,
                      String httpMethod,
                      Integer statusCode,
                      String message,
                      String traceId) {
        this.id = id;
        this.timestamp = timestamp;
        this.serviceName = serviceName;
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.statusCode = statusCode;
        this.message = message;
        this.traceId = traceId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}


