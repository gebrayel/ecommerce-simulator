package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.model.Payment;
import com.tuempresa.ecommerce.orders.domain.port.in.PaymentUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderRepositoryPort;
import com.tuempresa.ecommerce.orders.domain.port.out.PaymentRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class PaymentService implements PaymentUseCase {

    private final PaymentRepositoryPort paymentRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;

    public PaymentService(PaymentRepositoryPort paymentRepositoryPort, OrderRepositoryPort orderRepositoryPort) {
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.orderRepositoryPort = orderRepositoryPort;
    }

    @Override
    public Payment registerPayment(Payment payment) {
        Order order = orderRepositoryPort.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + payment.getOrderId()));

        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        Payment savedPayment = paymentRepositoryPort.save(payment);

        if (Payment.Status.COMPLETED.equals(payment.getStatus())) {
            order.markAsPaid();
            orderRepositoryPort.save(order);
        }

        return savedPayment;
    }

    @Override
    public Payment markAsCompleted(Long paymentId) {
        Payment payment = getOrThrow(paymentId);
        payment.markCompleted();
        payment.setUpdatedAt(LocalDateTime.now());
        Payment savedPayment = paymentRepositoryPort.save(payment);

        Order order = orderRepositoryPort.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + payment.getOrderId()));
        order.markAsPaid();
        orderRepositoryPort.save(order);

        return savedPayment;
    }

    @Override
    public Payment markAsFailed(Long paymentId) {
        Payment payment = getOrThrow(paymentId);
        payment.markFailed();
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepositoryPort.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment findById(Long paymentId) {
        return getOrThrow(paymentId);
    }

    private Payment getOrThrow(Long paymentId) {
        return paymentRepositoryPort.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + paymentId));
    }
}


