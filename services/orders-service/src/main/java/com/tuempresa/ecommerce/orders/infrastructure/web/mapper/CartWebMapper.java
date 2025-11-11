package com.tuempresa.ecommerce.orders.infrastructure.web.mapper;

import com.tuempresa.ecommerce.orders.domain.model.Cart;
import com.tuempresa.ecommerce.orders.domain.model.CartItem;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CartItemResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CartResponse;

import java.util.List;
import java.util.stream.Collectors;

public class CartWebMapper {

    private CartWebMapper() {
    }

    public static CartResponse toResponse(Cart cart) {
        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                cart.getUserEmail(),
                cart.getUserName(),
                cart.getUserPhone(),
                cart.getUserAddress(),
                toItemResponses(cart.getItems()),
                cart.getTotal(),
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }

    private static List<CartItemResponse> toItemResponses(List<CartItem> items) {
        return items.stream()
                .map(item -> new CartItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getProductDescription(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
    }
}


