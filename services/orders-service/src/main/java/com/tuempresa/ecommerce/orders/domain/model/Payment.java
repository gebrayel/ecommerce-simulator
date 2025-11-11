package com.tuempresa.ecommerce.orders.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {

    public enum Status {
        PENDING,
        COMPLETED,
        FAILED
    }

    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String method;
    private Status status;
    private Long creditCardId;
    private String cardTokenId;
    private String cardLastFour;
    private int attempts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Payment() {
    }

    public Payment(Long id,
                   Long orderId,
                   BigDecimal amount,
                   String method,
                   Status status,
                   Long creditCardId,
                   String cardTokenId,
                   String cardLastFour,
                   int attempts,
                   LocalDateTime createdAt,
                   LocalDateTime updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.creditCardId = creditCardId;
        this.cardTokenId = cardTokenId;
        this.cardLastFour = cardLastFour;
        this.attempts = attempts;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Payment(Long orderId, BigDecimal amount, String method, Long creditCardId, String cardTokenId, String cardLastFour) {
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.creditCardId = creditCardId;
        this.cardTokenId = cardTokenId;
        this.cardLastFour = cardLastFour;
        this.status = Status.PENDING;
        this.attempts = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public Long getCreditCardId() {
        return creditCardId;
    }

    public void setCreditCardId(Long creditCardId) {
        this.creditCardId = creditCardId;
    }

    public String getCardTokenId() {
        return cardTokenId;
    }

    public void setCardTokenId(String cardTokenId) {
        this.cardTokenId = cardTokenId;
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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

    public void markCompleted() {
        this.status = Status.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = Status.FAILED;
        this.updatedAt = LocalDateTime.now();
    }
}


