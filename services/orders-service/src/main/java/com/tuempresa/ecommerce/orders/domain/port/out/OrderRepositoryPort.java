package com.tuempresa.ecommerce.orders.domain.port.out;

import com.tuempresa.ecommerce.orders.domain.model.Order;

import java.util.Optional;

public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findById(Long id);
}


