package com.tuempresa.ecommerce.orders.domain.port.in;

import com.tuempresa.ecommerce.orders.domain.model.Cart;

public interface CartUseCase {

    Cart createCart(Long userId);

    Cart addItem(Long cartId, Long productId, Integer quantity);

    Cart removeItem(Long cartId, Long productId);

    Cart clearCart(Long cartId);

    Cart getById(Long cartId);
}


