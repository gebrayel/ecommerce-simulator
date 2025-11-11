package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {

    @NotNull
    private Long cartId;

    @NotBlank
    private String deliveryAddress;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(Long cartId, String deliveryAddress) {
        this.cartId = cartId;
        this.deliveryAddress = deliveryAddress;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
}


