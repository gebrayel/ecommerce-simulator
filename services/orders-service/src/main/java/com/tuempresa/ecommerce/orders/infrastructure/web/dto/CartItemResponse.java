package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import java.math.BigDecimal;

public class CartItemResponse {

    private Long productId;
    private String productName;
    private String productDescription;
    private Integer quantity;
    private BigDecimal price;

    public CartItemResponse() {
    }

    public CartItemResponse(Long productId,
                            String productName,
                            String productDescription,
                            Integer quantity,
                            BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}


