package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

public class OrderSettingsResponse {

    private Double cardRejectionProbability;
    private Integer paymentRetryAttempts;

    public OrderSettingsResponse() {
    }

    public OrderSettingsResponse(Double cardRejectionProbability, Integer paymentRetryAttempts) {
        this.cardRejectionProbability = cardRejectionProbability;
        this.paymentRetryAttempts = paymentRetryAttempts;
    }

    public Double getCardRejectionProbability() {
        return cardRejectionProbability;
    }

    public void setCardRejectionProbability(Double cardRejectionProbability) {
        this.cardRejectionProbability = cardRejectionProbability;
    }

    public Integer getPaymentRetryAttempts() {
        return paymentRetryAttempts;
    }

    public void setPaymentRetryAttempts(Integer paymentRetryAttempts) {
        this.paymentRetryAttempts = paymentRetryAttempts;
    }
}


