package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import com.tuempresa.ecommerce.orders.domain.port.in.OrderUseCase;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreateOrderRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.OrderResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.mapper.OrderWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderUseCase orderUseCase;

    public OrderController(OrderUseCase orderUseCase) {
        this.orderUseCase = orderUseCase;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(OrderWebMapper.toResponse(orderUseCase.createFromCart(request.getCartId(), request.getDeliveryAddress())));
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<OrderResponse> markAsPaid(@PathVariable Long orderId) {
        return ResponseEntity.ok(
                OrderWebMapper.toResponse(orderUseCase.markAsPaid(orderId))
        );
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long orderId) {
        return ResponseEntity.ok(
                OrderWebMapper.toResponse(orderUseCase.cancel(orderId))
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long orderId) {
        return ResponseEntity.ok(
                OrderWebMapper.toResponse(orderUseCase.findById(orderId))
        );
    }
}


