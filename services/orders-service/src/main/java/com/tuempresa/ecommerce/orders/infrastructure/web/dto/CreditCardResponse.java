package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import java.time.LocalDateTime;

public class CreditCardResponse {

    private Long id;
    private String token;
    private String lastFourDigits;
    private Integer expiryMonth;
    private Integer expiryYear;
    private LocalDateTime createdAt;

    public CreditCardResponse() {
    }

    public CreditCardResponse(Long id,
                              String token,
                              String lastFourDigits,
                              Integer expiryMonth,
                              Integer expiryYear,
                              LocalDateTime createdAt) {
        this.id = id;
        this.token = token;
        this.lastFourDigits = lastFourDigits;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public void setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }

    public Integer getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(Integer expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public Integer getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(Integer expiryYear) {
        this.expiryYear = expiryYear;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


