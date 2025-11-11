package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import com.tuempresa.ecommerce.orders.application.service.security.ApiKeyValidator;
import com.tuempresa.ecommerce.orders.application.service.security.JwtService;
import com.tuempresa.ecommerce.orders.domain.model.CreditCard;
import com.tuempresa.ecommerce.orders.domain.port.in.CreditCardUseCase;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreateCreditCardRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreditCardResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.mapper.CreditCardWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cards")
public class CreditCardController {

    private final CreditCardUseCase creditCardUseCase;
    private final JwtService jwtService;
    private final ApiKeyValidator apiKeyValidator;

    public CreditCardController(CreditCardUseCase creditCardUseCase,
                                JwtService jwtService,
                                ApiKeyValidator apiKeyValidator) {
        this.creditCardUseCase = creditCardUseCase;
        this.jwtService = jwtService;
        this.apiKeyValidator = apiKeyValidator;
    }

    @PostMapping
    public ResponseEntity<CreditCardResponse> registerCard(@RequestHeader("x-api-key") String apiKey,
                                                           @RequestHeader("Authorization") String authorization,
                                                           @Valid @RequestBody CreateCreditCardRequest request) {
        apiKeyValidator.validate(apiKey);
        Long userId = jwtService.extractUserId(authorization);
        CreditCard card = creditCardUseCase.registerCard(
                userId,
                request.getCardNumber(),
                request.getCvv(),
                request.getExpiryMonth(),
                request.getExpiryYear()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(CreditCardWebMapper.toResponse(card));
    }

    @GetMapping
    public ResponseEntity<List<CreditCardResponse>> listUserCards(@RequestHeader("x-api-key") String apiKey,
                                                                  @RequestHeader("Authorization") String authorization) {
        apiKeyValidator.validate(apiKey);
        Long userId = jwtService.extractUserId(authorization);
        List<CreditCardResponse> responses = creditCardUseCase.findByUser(userId).stream()
                .map(CreditCardWebMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }
}


