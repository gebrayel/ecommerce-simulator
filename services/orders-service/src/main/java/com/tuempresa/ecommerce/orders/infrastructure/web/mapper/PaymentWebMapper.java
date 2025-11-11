package com.tuempresa.ecommerce.orders.infrastructure.web.mapper;

import com.tuempresa.ecommerce.orders.domain.model.Payment;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreatePaymentRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.PaymentResponse;

public class PaymentWebMapper {

    private PaymentWebMapper() {
    }

    public static Payment toDomain(CreatePaymentRequest request) {
        return new Payment(
                request.getOrderId(),
                request.getAmount(),
                request.getMethod()
        );
    }

    public static PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}


