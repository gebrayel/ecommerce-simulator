package com.tuempresa.ecommerce.orders.domain.model;

public class OrderSettings {

    private Long id;
    private Double cardRejectionProbability;
    private Integer paymentRetryAttempts;

    public OrderSettings() {
    }

    public OrderSettings(Long id, Double cardRejectionProbability, Integer paymentRetryAttempts) {
        this.id = id;
        this.cardRejectionProbability = cardRejectionProbability;
        this.paymentRetryAttempts = paymentRetryAttempts;
    }

    public OrderSettings(Double cardRejectionProbability, Integer paymentRetryAttempts) {
        this.cardRejectionProbability = cardRejectionProbability;
        this.paymentRetryAttempts = paymentRetryAttempts;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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


