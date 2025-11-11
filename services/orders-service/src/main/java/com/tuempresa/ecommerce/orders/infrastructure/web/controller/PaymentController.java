package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import com.tuempresa.ecommerce.orders.domain.port.in.PaymentUseCase;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreatePaymentRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.PaymentResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.mapper.PaymentWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentUseCase paymentUseCase;

    public PaymentController(PaymentUseCase paymentUseCase) {
        this.paymentUseCase = paymentUseCase;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> registerPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PaymentWebMapper.toResponse(paymentUseCase.registerPayment(PaymentWebMapper.toDomain(request))));
    }

    @PostMapping("/{paymentId}/complete")
    public ResponseEntity<PaymentResponse> markAsCompleted(@PathVariable Long paymentId) {
        return ResponseEntity.ok(
                PaymentWebMapper.toResponse(paymentUseCase.markAsCompleted(paymentId))
        );
    }

    @PostMapping("/{paymentId}/fail")
    public ResponseEntity<PaymentResponse> markAsFailed(@PathVariable Long paymentId) {
        return ResponseEntity.ok(
                PaymentWebMapper.toResponse(paymentUseCase.markAsFailed(paymentId))
        );
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> findById(@PathVariable Long paymentId) {
        return ResponseEntity.ok(
                PaymentWebMapper.toResponse(paymentUseCase.findById(paymentId))
        );
    }
}


