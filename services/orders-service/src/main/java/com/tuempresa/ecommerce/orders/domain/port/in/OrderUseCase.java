package com.tuempresa.ecommerce.orders.domain.port.in;

import com.tuempresa.ecommerce.orders.domain.model.Order;

public interface OrderUseCase {

    Order createFromCart(Long cartId, String deliveryAddress);

    Order findById(Long orderId);

    Order markAsPaid(Long orderId);

    Order cancel(Long orderId);
}


