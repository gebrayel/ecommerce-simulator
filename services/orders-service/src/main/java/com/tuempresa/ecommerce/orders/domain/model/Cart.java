package com.tuempresa.ecommerce.orders.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Cart {

    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private String userPhone;
    private String userAddress;
    private List<CartItem> items;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Cart() {
        this.items = new ArrayList<>();
        this.total = BigDecimal.ZERO;
    }

    public Cart(Long id,
                Long userId,
                String userEmail,
                String userName,
                String userPhone,
                String userAddress,
                List<CartItem> items,
                BigDecimal total,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userAddress = userAddress;
        this.items = new ArrayList<>(items);
        this.total = total;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Cart(Long userId,
                String userEmail,
                String userName,
                String userPhone,
                String userAddress) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userAddress = userAddress;
        this.items = new ArrayList<>();
        this.total = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        if (items == null) {
            this.items = new ArrayList<>();
            this.total = BigDecimal.ZERO;
            return;
        }
        this.items = new ArrayList<>(items);
        recalculateTotal();
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void addItem(CartItem item) {
        this.items.add(item);
        recalculateTotal();
        this.updatedAt = LocalDateTime.now();
    }

    public void removeItem(Long productId) {
        this.items.removeIf(item -> item.getProductId().equals(productId));
        recalculateTotal();
        this.updatedAt = LocalDateTime.now();
    }

    public void clearItems() {
        this.items.clear();
        this.total = BigDecimal.ZERO;
        this.updatedAt = LocalDateTime.now();
    }

    private void recalculateTotal() {
        this.total = this.items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}


