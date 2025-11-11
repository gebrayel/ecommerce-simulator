package com.tuempresa.ecommerce.orders.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_settings")
public class OrderSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_rejection_probability", nullable = false)
    private Double cardRejectionProbability;

    @Column(name = "payment_retry_attempts", nullable = false)
    private Integer paymentRetryAttempts;

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


