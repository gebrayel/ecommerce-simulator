package com.tuempresa.ecommerce.orders.domain.port.out;

import com.tuempresa.ecommerce.orders.domain.model.Cart;

import java.util.Optional;

public interface CartRepositoryPort {

    Cart save(Cart cart);

    Optional<Cart> findById(Long id);

    void deleteById(Long id);
}


