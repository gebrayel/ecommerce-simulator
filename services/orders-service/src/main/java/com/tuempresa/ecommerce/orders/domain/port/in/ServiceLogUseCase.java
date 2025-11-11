package com.tuempresa.ecommerce.orders.domain.port.in;

public interface ServiceLogUseCase {

    void log(String traceId,
             String endpoint,
             String httpMethod,
             Integer statusCode,
             String message);
}


