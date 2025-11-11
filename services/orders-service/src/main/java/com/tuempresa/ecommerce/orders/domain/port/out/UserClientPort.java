package com.tuempresa.ecommerce.orders.domain.port.out;

import com.tuempresa.ecommerce.orders.domain.model.UserSnapshot;

import java.util.Optional;

public interface UserClientPort {

    Optional<UserSnapshot> findById(Long userId);
}


