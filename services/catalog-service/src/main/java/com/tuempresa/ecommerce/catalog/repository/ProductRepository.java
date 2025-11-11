package com.tuempresa.ecommerce.catalog.repository;

import com.tuempresa.ecommerce.catalog.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}

