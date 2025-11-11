package com.tuempresa.ecommerce.orders.infrastructure.persistence.adapter;

import com.tuempresa.ecommerce.orders.domain.model.Payment;
import com.tuempresa.ecommerce.orders.domain.port.out.PaymentRepositoryPort;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.PaymentEntity;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper.PaymentMapper;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.repository.PaymentJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final PaymentJpaRepository paymentJpaRepository;

    public PaymentRepositoryAdapter(PaymentJpaRepository paymentJpaRepository) {
        this.paymentJpaRepository = paymentJpaRepository;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = PaymentMapper.toEntity(payment);
        PaymentEntity saved = paymentJpaRepository.save(entity);
        return PaymentMapper.toDomain(saved);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return paymentJpaRepository.findById(id)
                .map(PaymentMapper::toDomain);
    }
}


