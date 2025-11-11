package com.tuempresa.ecommerce.catalog.application.service;

import com.tuempresa.ecommerce.catalog.domain.exception.DuplicateResourceException;
import com.tuempresa.ecommerce.catalog.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.catalog.domain.model.PaginatedResult;
import com.tuempresa.ecommerce.catalog.domain.model.Product;
import com.tuempresa.ecommerce.catalog.domain.model.SearchQuery;
import com.tuempresa.ecommerce.catalog.domain.port.in.ProductUseCase;
import com.tuempresa.ecommerce.catalog.domain.port.out.ProductRepositoryPort;
import com.tuempresa.ecommerce.catalog.domain.port.out.SearchQueryRepositoryPort;
import com.tuempresa.ecommerce.catalog.domain.port.out.CatalogSettingsRepositoryPort;
import com.tuempresa.ecommerce.catalog.domain.port.out.CatalogSettingsRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class ProductService implements ProductUseCase {

    private final ProductRepositoryPort productRepositoryPort;
    private final SearchQueryRepositoryPort searchQueryRepositoryPort;
    private final CatalogSettingsRepositoryPort catalogSettingsRepositoryPort;

    public ProductService(ProductRepositoryPort productRepositoryPort,
                          SearchQueryRepositoryPort searchQueryRepositoryPort,
                          CatalogSettingsRepositoryPort catalogSettingsRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
        this.searchQueryRepositoryPort = searchQueryRepositoryPort;
        this.catalogSettingsRepositoryPort = catalogSettingsRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<Product> findAll(int page, int size, String search) {
        int sanitizedPage = Math.max(page, 0);
        int sanitizedSize = size <= 0 ? 10 : size;
        String normalizedSearch = search != null && !search.trim().isEmpty() ? search.trim() : null;

        int minimumStock = catalogSettingsRepositoryPort.findSettings()
                .map(settings -> settings.getMinimumStock() != null ? settings.getMinimumStock() : 0)
                .orElse(0);

        PaginatedResult<Product> result = productRepositoryPort.findAll(sanitizedPage, sanitizedSize, normalizedSearch, minimumStock);

        SearchQuery searchQuery = new SearchQuery(normalizedSearch, sanitizedPage, sanitizedSize);
        searchQueryRepositoryPort.save(searchQuery);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return productRepositoryPort.findById(id);
    }

    @Override
    public Product create(Product product) {
        productRepositoryPort.findByName(product.getName())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Ya existe un producto con el nombre: " + product.getName());
                });
        product.setDeleted(false);
        product.setCreatedAt(Optional.ofNullable(product.getCreatedAt()).orElse(LocalDateTime.now()));
        product.setUpdatedAt(Optional.ofNullable(product.getUpdatedAt()).orElse(LocalDateTime.now()));
        return productRepositoryPort.save(product);
    }

    @Override
    public Product update(Long id, Product product) {
        Product existingProduct = productRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        if (!existingProduct.getName().equalsIgnoreCase(product.getName())
                && productRepositoryPort.existsByNameExcludingId(product.getName(), id)) {
            throw new DuplicateResourceException("Ya existe un producto con el nombre: " + product.getName());
        }

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStock(product.getStock());
        existingProduct.setUpdatedAt(LocalDateTime.now());

        return productRepositoryPort.save(existingProduct);
    }

    @Override
    public void delete(Long id) {
        productRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        productRepositoryPort.softDelete(id);
    }
}


