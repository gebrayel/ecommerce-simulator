package com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper;

import com.tuempresa.ecommerce.orders.domain.model.Cart;
import com.tuempresa.ecommerce.orders.domain.model.CartItem;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.CartEntity;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.CartItemEmbeddable;

import java.util.List;
import java.util.stream.Collectors;

public class CartMapper {

    private CartMapper() {
    }

    public static CartEntity toEntity(Cart cart) {
        CartEntity entity = new CartEntity();
        entity.setId(cart.getId());
        entity.setUserId(cart.getUserId());
        entity.setUserEmail(cart.getUserEmail());
        entity.setUserName(cart.getUserName());
        entity.setUserPhone(cart.getUserPhone());
        entity.setUserAddress(cart.getUserAddress());
        entity.setItems(toEmbeddableList(cart.getItems()));
        entity.setTotal(cart.getTotal());
        entity.setCreatedAt(cart.getCreatedAt());
        entity.setUpdatedAt(cart.getUpdatedAt());
        return entity;
    }

    public static Cart toDomain(CartEntity entity) {
        return new Cart(
                entity.getId(),
                entity.getUserId(),
                entity.getUserEmail(),
                entity.getUserName(),
                entity.getUserPhone(),
                entity.getUserAddress(),
                toDomainList(entity.getItems()),
                entity.getTotal(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private static List<CartItemEmbeddable> toEmbeddableList(List<CartItem> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .map(item -> new CartItemEmbeddable(
                        item.getProductId(),
                        item.getProductName(),
                        item.getProductDescription(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
    }

    private static List<CartItem> toDomainList(List<CartItemEmbeddable> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .map(item -> new CartItem(
                        item.getProductId(),
                        item.getProductName(),
                        item.getProductDescription(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
    }
}


