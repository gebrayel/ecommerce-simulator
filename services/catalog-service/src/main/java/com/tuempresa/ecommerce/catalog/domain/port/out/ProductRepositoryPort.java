package com.tuempresa.ecommerce.catalog.domain.port.out;

import com.tuempresa.ecommerce.catalog.domain.model.PaginatedResult;
import com.tuempresa.ecommerce.catalog.domain.model.Product;

import java.util.Optional;

public interface ProductRepositoryPort {

    PaginatedResult<Product> findAll(int page, int size, String search, int minimumStock);

    Optional<Product> findById(Long id);

    Optional<Product> findByName(String name);

    Product save(Product product);

    void softDelete(Long id);

    boolean existsByNameExcludingId(String name, Long excludedId);
}


