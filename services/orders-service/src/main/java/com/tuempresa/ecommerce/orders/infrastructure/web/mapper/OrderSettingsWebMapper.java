package com.tuempresa.ecommerce.orders.infrastructure.web.mapper;

import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.OrderSettingsRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.OrderSettingsResponse;

public class OrderSettingsWebMapper {

    private OrderSettingsWebMapper() {
    }

    public static OrderSettingsResponse toResponse(OrderSettings settings) {
        if (settings == null) {
            return null;
        }
        return new OrderSettingsResponse(
                settings.getCardRejectionProbability(),
                settings.getPaymentRetryAttempts()
        );
    }

    public static OrderSettings toDomain(OrderSettingsRequest request) {
        if (request == null) {
            return null;
        }
        return new OrderSettings(
                request.getCardRejectionProbability(),
                request.getPaymentRetryAttempts()
        );
    }
}


