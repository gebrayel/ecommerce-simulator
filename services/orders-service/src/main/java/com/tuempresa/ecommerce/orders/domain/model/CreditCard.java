package com.tuempresa.ecommerce.orders.domain.model;

import java.time.LocalDateTime;

public class CreditCard {

    private Long id;
    private Long userId;
    private String cardNumberHash;
    private String lastFourDigits;
    private Integer expiryMonth;
    private Integer expiryYear;
    private String tokenId;
    private String tokenSignature;
    private LocalDateTime createdAt;
    private transient String plainToken;

    public CreditCard() {
    }

    public CreditCard(Long id,
                      Long userId,
                      String cardNumberHash,
                      String lastFourDigits,
                      Integer expiryMonth,
                      Integer expiryYear,
                      String tokenId,
                      String tokenSignature,
                      LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.cardNumberHash = cardNumberHash;
        this.lastFourDigits = lastFourDigits;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.tokenId = tokenId;
        this.tokenSignature = tokenSignature;
        this.createdAt = createdAt;
    }

    public CreditCard(Long userId,
                      String cardNumberHash,
                      String lastFourDigits,
                      Integer expiryMonth,
                      Integer expiryYear) {
        this.userId = userId;
        this.cardNumberHash = cardNumberHash;
        this.lastFourDigits = lastFourDigits;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCardNumberHash() {
        return cardNumberHash;
    }

    public void setCardNumberHash(String cardNumberHash) {
        this.cardNumberHash = cardNumberHash;
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

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getTokenSignature() {
        return tokenSignature;
    }

    public void setTokenSignature(String tokenSignature) {
        this.tokenSignature = tokenSignature;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPlainToken() {
        return plainToken;
    }

    public void setPlainToken(String plainToken) {
        this.plainToken = plainToken;
    }
}


