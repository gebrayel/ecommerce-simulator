package com.tuempresa.ecommerce.orders.domain.port.out;

import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;

import java.util.Optional;

public interface OrderSettingsRepositoryPort {

    Optional<OrderSettings> findSettings();

    OrderSettings save(OrderSettings settings);
}


