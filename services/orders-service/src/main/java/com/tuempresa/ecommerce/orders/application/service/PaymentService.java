package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.exception.UnauthorizedAccessException;
import com.tuempresa.ecommerce.orders.domain.model.CreditCard;
import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.model.Payment;
import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;
import com.tuempresa.ecommerce.orders.domain.port.in.CreditCardUseCase;
import com.tuempresa.ecommerce.orders.domain.port.in.OrderSettingsUseCase;
import com.tuempresa.ecommerce.orders.domain.port.in.PaymentUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderRepositoryPort;
import com.tuempresa.ecommerce.orders.domain.port.out.PaymentRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class PaymentService implements PaymentUseCase {

    private final PaymentRepositoryPort paymentRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final CreditCardUseCase creditCardUseCase;
    private final OrderSettingsUseCase orderSettingsUseCase;

    public PaymentService(PaymentRepositoryPort paymentRepositoryPort,
                          OrderRepositoryPort orderRepositoryPort,
                          CreditCardUseCase creditCardUseCase,
                          OrderSettingsUseCase orderSettingsUseCase) {
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.orderRepositoryPort = orderRepositoryPort;
        this.creditCardUseCase = creditCardUseCase;
        this.orderSettingsUseCase = orderSettingsUseCase;
    }

    @Override
    public Payment registerPayment(Long userId,
                                   Long orderId,
                                   BigDecimal amount,
                                   String method,
                                   String cardToken) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("El usuario autenticado no coincide con la orden");
        }

        CreditCard creditCard = creditCardUseCase.findByToken(cardToken)
                .orElseThrow(() -> new UnauthorizedAccessException("Token de tarjeta inválido"));

        if (!creditCard.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("La tarjeta no pertenece al usuario autenticado");
        }

        ensureCardNotExpired(creditCard);

        Payment payment = new Payment(
                orderId,
                amount,
                method,
                creditCard.getId(),
                creditCard.getTokenId(),
                creditCard.getLastFourDigits()
        );

        OrderSettings settings = orderSettingsUseCase.getSettings();
        double rejectionProbability = settings.getCardRejectionProbability() != null
                ? clampProbability(settings.getCardRejectionProbability())
                : 0.0d;
        int maxRetries = settings.getPaymentRetryAttempts() != null && settings.getPaymentRetryAttempts() > 0
                ? settings.getPaymentRetryAttempts()
                : 1;

        int attempts = 0;
        boolean approved = false;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        while (attempts < maxRetries) {
            attempts++;
            double roll = random.nextDouble();
            if (roll >= rejectionProbability) {
                approved = true;
                break;
            }
        }

        payment.setAttempts(attempts);

        if (approved) {
            payment.markCompleted();
            Payment savedPayment = paymentRepositoryPort.save(payment);
            order.markAsPaid();
            orderRepositoryPort.save(order);
            return savedPayment;
        } else {
            payment.markFailed();
            return paymentRepositoryPort.save(payment);
        }
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

    private void ensureCardNotExpired(CreditCard creditCard) {
        YearMonth expiry = YearMonth.of(creditCard.getExpiryYear(), creditCard.getExpiryMonth());
        if (expiry.isBefore(YearMonth.now())) {
            throw new UnauthorizedAccessException("La tarjeta se encuentra vencida");
        }
    }

    private double clampProbability(Double value) {
        if (value == null) {
            return 0.0d;
        }
        if (value < 0) {
            return 0.0d;
        }
        if (value > 1) {
            return 1.0d;
    }
        return value;
    }
}


