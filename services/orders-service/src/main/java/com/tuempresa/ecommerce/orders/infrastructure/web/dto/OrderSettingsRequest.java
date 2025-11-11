package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderSettingsRequest {

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "1.0", inclusive = true)
    private Double cardRejectionProbability;

    @NotNull
    @Min(1)
    private Integer paymentRetryAttempts;

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


