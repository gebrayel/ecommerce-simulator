package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;

public class CreateCartRequest {

    @NotNull
    private Long userId;

    public CreateCartRequest() {
    }

    public CreateCartRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}


