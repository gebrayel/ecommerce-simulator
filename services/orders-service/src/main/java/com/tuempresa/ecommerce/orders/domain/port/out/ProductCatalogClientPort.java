package com.tuempresa.ecommerce.orders.domain.port.out;

import com.tuempresa.ecommerce.orders.domain.model.ProductSnapshot;

import java.util.Optional;

public interface ProductCatalogClientPort {

    Optional<ProductSnapshot> findById(Long productId);
}


