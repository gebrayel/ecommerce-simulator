package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.exception.InvalidOrderStateException;
import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.model.Cart;
import com.tuempresa.ecommerce.orders.domain.model.CartItem;
import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.model.OrderItem;
import com.tuempresa.ecommerce.orders.domain.port.in.OrderUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.CartRepositoryPort;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService implements OrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final CartRepositoryPort cartRepositoryPort;

    public OrderService(OrderRepositoryPort orderRepositoryPort, CartRepositoryPort cartRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.cartRepositoryPort = cartRepositoryPort;
    }

    @Override
    public Order createFromCart(Long cartId) {
        Cart cart = cartRepositoryPort.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado con ID: " + cartId));

        if (cart.getItems().isEmpty()) {
            throw new InvalidOrderStateException("No se puede crear una orden con un carrito vacío");
        }

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(this::toOrderItem)
                .collect(Collectors.toList());

        BigDecimal total = orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order(cart.getUserId(), orderItems, total);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepositoryPort.save(order);

        cart.clearItems();
        cartRepositoryPort.save(cart);

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order findById(Long orderId) {
        return orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + orderId));
    }

    @Override
    public Order markAsPaid(Long orderId) {
        Order order = findById(orderId);
        if (Order.Status.CANCELLED.equals(order.getStatus())) {
            throw new InvalidOrderStateException("No se puede marcar como pagada una orden cancelada");
        }
        order.markAsPaid();
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepositoryPort.save(order);
    }

    @Override
    public Order cancel(Long orderId) {
        Order order = findById(orderId);
        if (Order.Status.PAID.equals(order.getStatus()) || Order.Status.COMPLETED.equals(order.getStatus())) {
            throw new InvalidOrderStateException("No se puede cancelar una orden pagada o completada");
        }
        order.cancel();
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepositoryPort.save(order);
    }

    private OrderItem toOrderItem(CartItem cartItem) {
        return new OrderItem(
                cartItem.getProductId(),
                cartItem.getProductName(),
                cartItem.getQuantity(),
                cartItem.getPrice()
        );
    }
}


