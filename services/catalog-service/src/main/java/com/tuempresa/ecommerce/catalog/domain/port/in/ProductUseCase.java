package com.tuempresa.ecommerce.catalog.domain.port.in;

import com.tuempresa.ecommerce.catalog.domain.model.PaginatedResult;
import com.tuempresa.ecommerce.catalog.domain.model.Product;

import java.util.Optional;

public interface ProductUseCase {

    PaginatedResult<Product> findAll(int page, int size, String search);

    Optional<Product> findById(Long id);

    Product create(Product product);

    Product update(Long id, Product product);

    void delete(Long id);
}


