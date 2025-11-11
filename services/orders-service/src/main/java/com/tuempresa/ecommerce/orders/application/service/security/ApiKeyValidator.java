package com.tuempresa.ecommerce.orders.application.service.security;

import com.tuempresa.ecommerce.orders.domain.exception.UnauthorizedAccessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyValidator {

    private final String expectedApiKey;

    public ApiKeyValidator(@Value("${security.api-key}") String expectedApiKey) {
        this.expectedApiKey = expectedApiKey;
    }

    public void validate(String providedKey) {
        if (expectedApiKey == null || expectedApiKey.isBlank()) {
            throw new IllegalStateException("API key esperada no configurada");
        }

        if (providedKey == null || providedKey.isBlank()) {
            throw new UnauthorizedAccessException("x-api-key faltante");
        }

        if (!expectedApiKey.equals(providedKey)) {
            throw new UnauthorizedAccessException("x-api-key inválida");
        }
    }
}


