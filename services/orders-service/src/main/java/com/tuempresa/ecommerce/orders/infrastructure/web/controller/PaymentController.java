package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import com.tuempresa.ecommerce.orders.application.service.security.ApiKeyValidator;
import com.tuempresa.ecommerce.orders.application.service.security.JwtService;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentUseCase paymentUseCase;
    private final JwtService jwtService;
    private final ApiKeyValidator apiKeyValidator;

    public PaymentController(PaymentUseCase paymentUseCase,
                              JwtService jwtService,
                              ApiKeyValidator apiKeyValidator) {
        this.paymentUseCase = paymentUseCase;
        this.jwtService = jwtService;
        this.apiKeyValidator = apiKeyValidator;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> registerPayment(@RequestHeader("x-api-key") String apiKey,
                                                           @RequestHeader("Authorization") String authorization,
                                                           @Valid @RequestBody CreatePaymentRequest request) {
        apiKeyValidator.validate(apiKey);
        Long userId = jwtService.extractUserId(authorization);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PaymentWebMapper.toResponse(
                        paymentUseCase.registerPayment(
                                userId,
                                request.getOrderId(),
                                request.getAmount(),
                                request.getMethod(),
                                request.getCardToken()
                        )
                ));
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

