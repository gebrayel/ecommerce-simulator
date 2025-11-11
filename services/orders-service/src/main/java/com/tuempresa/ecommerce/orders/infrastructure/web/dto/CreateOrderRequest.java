package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {

    @NotNull
    private Long cartId;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(Long cartId) {
        this.cartId = cartId;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }
}


