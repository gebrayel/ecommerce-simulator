package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CreateCreditCardRequest {

    @NotBlank
    @Pattern(regexp = "\\d{12,19}", message = "El número de tarjeta debe contener entre 12 y 19 dígitos")
    private String cardNumber;

    @NotBlank
    @Pattern(regexp = "\\d{3,4}", message = "El CVV debe contener 3 o 4 dígitos")
    private String cvv;

    @NotNull
    @Min(1)
    @Max(12)
    private Integer expiryMonth;

    @NotNull
    private Integer expiryYear;

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
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
}


