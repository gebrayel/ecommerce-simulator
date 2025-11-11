package com.tuempresa.ecommerce.catalog.infrastructure.persistence.mapper;

import com.tuempresa.ecommerce.catalog.domain.model.Product;
import com.tuempresa.ecommerce.catalog.infrastructure.persistence.entity.ProductEntity;

public class ProductEntityMapper {

    private ProductEntityMapper() {
    }

    public static Product toDomain(ProductEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Product(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getStock(),
                entity.isDeleted(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static ProductEntity toEntity(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductEntity(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.isDeleted(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}


