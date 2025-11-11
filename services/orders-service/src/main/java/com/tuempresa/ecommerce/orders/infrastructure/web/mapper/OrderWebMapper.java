package com.tuempresa.ecommerce.orders.infrastructure.web.mapper;

import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.model.OrderItem;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.OrderItemResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.OrderResponse;

import java.util.List;
import java.util.stream.Collectors;

public class OrderWebMapper {

    private OrderWebMapper() {
    }

    public static OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getDeliveryAddress(),
                toItemResponses(order.getItems()),
                order.getTotal(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private static List<OrderItemResponse> toItemResponses(List<OrderItem> items) {
        return items.stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
    }
}


