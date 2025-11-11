package com.tuempresa.ecommerce.catalog.infrastructure.persistence.adapter;

import com.tuempresa.ecommerce.catalog.domain.model.PaginatedResult;
import com.tuempresa.ecommerce.catalog.domain.model.Product;
import com.tuempresa.ecommerce.catalog.domain.port.out.ProductRepositoryPort;
import com.tuempresa.ecommerce.catalog.infrastructure.persistence.entity.ProductEntity;
import com.tuempresa.ecommerce.catalog.infrastructure.persistence.mapper.ProductEntityMapper;
import com.tuempresa.ecommerce.catalog.infrastructure.persistence.repository.ProductJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository productJpaRepository;

    public ProductRepositoryAdapter(ProductJpaRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public PaginatedResult<Product> findAll(int page, int size, String search, int minimumStock) {
        int sanitizedPage = Math.max(page, 0);
        int sanitizedSize = size <= 0 ? 10 : size;
        int sanitizedMinimumStock = Math.max(minimumStock, 0);

        Page<ProductEntity> productPage;
        if (search != null && !search.isBlank()) {
            productPage = productJpaRepository.searchByNameOrDescription(search.trim(), sanitizedMinimumStock, PageRequest.of(sanitizedPage, sanitizedSize));
        } else {
            productPage = productJpaRepository.findAllByDeletedFalseWithMinimumStock(sanitizedMinimumStock, PageRequest.of(sanitizedPage, sanitizedSize));
        }
        return new PaginatedResult<>(
                productPage.getContent().stream()
                        .map(ProductEntityMapper::toDomain)
                        .collect(Collectors.toList()),
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findByIdAndDeletedFalse(id)
                .map(ProductEntityMapper::toDomain);
    }

    @Override
    public Optional<Product> findByName(String name) {
        return productJpaRepository.findByNameIgnoreCaseAndDeletedFalse(name)
                .map(ProductEntityMapper::toDomain);
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = ProductEntityMapper.toEntity(product);
        if (entity.getId() != null) {
            productJpaRepository.findById(entity.getId()).ifPresent(existing -> entity.setCreatedAt(existing.getCreatedAt()));
        }
        ProductEntity saved = productJpaRepository.save(entity);
        return ProductEntityMapper.toDomain(saved);
    }

    @Override
    public void softDelete(Long id) {
        productJpaRepository.findById(id).ifPresent(entity -> {
            entity.setDeleted(true);
            entity.setUpdatedAt(LocalDateTime.now());
            productJpaRepository.save(entity);
        });
    }

    @Override
    public boolean existsByNameExcludingId(String name, Long excludedId) {
        return productJpaRepository.existsByNameIgnoreCaseAndIdNotAndDeletedFalse(name, excludedId);
    }
}


