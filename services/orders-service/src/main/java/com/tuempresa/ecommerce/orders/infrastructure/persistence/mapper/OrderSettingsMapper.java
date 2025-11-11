package com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper;

import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.OrderSettingsEntity;

public class OrderSettingsMapper {

    private OrderSettingsMapper() {
    }

    public static OrderSettings toDomain(OrderSettingsEntity entity) {
        if (entity == null) {
            return null;
        }
        return new OrderSettings(
                entity.getId(),
                entity.getCardRejectionProbability(),
                entity.getPaymentRetryAttempts()
        );
    }

    public static OrderSettingsEntity toEntity(OrderSettings settings) {
        if (settings == null) {
            return null;
        }
        OrderSettingsEntity entity = new OrderSettingsEntity();
        entity.setId(settings.getId());
        entity.setCardRejectionProbability(settings.getCardRejectionProbability());
        entity.setPaymentRetryAttempts(settings.getPaymentRetryAttempts());
        return entity;
    }
}


