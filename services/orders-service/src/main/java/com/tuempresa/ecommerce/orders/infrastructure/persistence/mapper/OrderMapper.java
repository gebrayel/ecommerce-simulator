package com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper;

import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.model.OrderItem;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.OrderEntity;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.OrderItemEmbeddable;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    private OrderMapper() {
    }

    public static OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId());
        entity.setUserId(order.getUserId());
        entity.setDeliveryAddress(order.getDeliveryAddress());
        entity.setItems(toEmbeddableList(order.getItems()));
        entity.setTotal(order.getTotal());
        entity.setStatus(order.getStatus());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());
        return entity;
    }

    public static Order toDomain(OrderEntity entity) {
        return new Order(
                entity.getId(),
                entity.getUserId(),
                entity.getDeliveryAddress(),
                toDomainList(entity.getItems()),
                entity.getTotal(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private static List<OrderItemEmbeddable> toEmbeddableList(List<OrderItem> items) {
        return items.stream()
                .map(item -> new OrderItemEmbeddable(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
    }

    private static List<OrderItem> toDomainList(List<OrderItemEmbeddable> items) {
        return items.stream()
                .map(item -> new OrderItem(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
    }
}


