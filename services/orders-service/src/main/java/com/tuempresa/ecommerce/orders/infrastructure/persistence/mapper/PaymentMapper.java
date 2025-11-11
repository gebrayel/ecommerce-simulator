package com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper;

import com.tuempresa.ecommerce.orders.domain.model.Payment;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.PaymentEntity;

public class PaymentMapper {

    private PaymentMapper() {
    }

    public static PaymentEntity toEntity(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.setId(payment.getId());
        entity.setOrderId(payment.getOrderId());
        entity.setAmount(payment.getAmount());
        entity.setMethod(payment.getMethod());
        entity.setStatus(payment.getStatus());
        entity.setCreditCardId(payment.getCreditCardId());
        entity.setCardTokenId(payment.getCardTokenId());
        entity.setCardLastFour(payment.getCardLastFour());
        entity.setAttempts(payment.getAttempts());
        entity.setCreatedAt(payment.getCreatedAt());
        entity.setUpdatedAt(payment.getUpdatedAt());
        return entity;
    }

    public static Payment toDomain(PaymentEntity entity) {
        return new Payment(
                entity.getId(),
                entity.getOrderId(),
                entity.getAmount(),
                entity.getMethod(),
                entity.getStatus(),
                entity.getCreditCardId(),
                entity.getCardTokenId(),
                entity.getCardLastFour(),
                entity.getAttempts() != null ? entity.getAttempts() : 0,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}


