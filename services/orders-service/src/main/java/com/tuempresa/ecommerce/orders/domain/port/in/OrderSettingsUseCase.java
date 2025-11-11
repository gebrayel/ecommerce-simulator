package com.tuempresa.ecommerce.orders.domain.port.in;

import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;

public interface OrderSettingsUseCase {

    OrderSettings getSettings();

    OrderSettings updateSettings(OrderSettings settings);
}


