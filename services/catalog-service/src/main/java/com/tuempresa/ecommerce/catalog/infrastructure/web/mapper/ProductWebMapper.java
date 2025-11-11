package com.tuempresa.ecommerce.catalog.infrastructure.web.mapper;

import com.tuempresa.ecommerce.catalog.domain.model.Product;
import com.tuempresa.ecommerce.catalog.infrastructure.web.dto.ProductRequest;
import com.tuempresa.ecommerce.catalog.infrastructure.web.dto.ProductResponse;

import java.time.LocalDateTime;

public class ProductWebMapper {

    private ProductWebMapper() {
    }

    public static Product toDomain(ProductRequest request) {
        if (request == null) {
            return null;
        }
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setDeleted(false);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    public static ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}


