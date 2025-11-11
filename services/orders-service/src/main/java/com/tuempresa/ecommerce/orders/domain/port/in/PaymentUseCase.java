package com.tuempresa.ecommerce.orders.domain.port.in;

import com.tuempresa.ecommerce.orders.domain.model.Payment;

import java.math.BigDecimal;

public interface PaymentUseCase {

    Payment registerPayment(Long userId, Long orderId, BigDecimal amount, String method, String cardToken);

    Payment markAsCompleted(Long paymentId);

    Payment markAsFailed(Long paymentId);

    Payment findById(Long paymentId);
}


