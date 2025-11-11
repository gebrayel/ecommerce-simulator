package com.tuempresa.ecommerce.orders.domain.port.in;

import com.tuempresa.ecommerce.orders.domain.model.Payment;

public interface PaymentUseCase {

    Payment registerPayment(Payment payment);

    Payment markAsCompleted(Long paymentId);

    Payment markAsFailed(Long paymentId);

    Payment findById(Long paymentId);
}


