package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import com.tuempresa.ecommerce.orders.domain.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String method;
    private String cardLastFour;
    private Payment.Status status;
    private int attempts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PaymentResponse() {
    }

    public PaymentResponse(Long id,
                           Long orderId,
                           BigDecimal amount,
                           String method,
                           String cardLastFour,
                           Payment.Status status,
                           int attempts,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.cardLastFour = cardLastFour;
        this.status = status;
        this.attempts = attempts;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
    }

    public Payment.Status getStatus() {
        return status;
    }

    public void setStatus(Payment.Status status) {
        this.status = status;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}


