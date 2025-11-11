package com.tuempresa.ecommerce.catalog.infrastructure.web.controller;

import com.tuempresa.ecommerce.catalog.domain.model.PaginatedResult;
import com.tuempresa.ecommerce.catalog.domain.model.Product;
import com.tuempresa.ecommerce.catalog.domain.port.in.ProductUseCase;
import com.tuempresa.ecommerce.catalog.infrastructure.web.dto.PaginatedResponse;
import com.tuempresa.ecommerce.catalog.infrastructure.web.dto.ProductRequest;
import com.tuempresa.ecommerce.catalog.infrastructure.web.dto.ProductResponse;
import com.tuempresa.ecommerce.catalog.infrastructure.web.mapper.ProductWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/catalog/products")
public class ProductController {

    private final ProductUseCase productUseCase;

    public ProductController(ProductUseCase productUseCase) {
        this.productUseCase = productUseCase;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10;
        }

        PaginatedResult<Product> result = productUseCase.findAll(page, size, search);

        PaginatedResponse<ProductResponse> response = new PaginatedResponse<>(
                result.getContent().stream()
                        .map(ProductWebMapper::toResponse)
                        .collect(Collectors.toList()),
                result.getPage(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return productUseCase.findById(id)
                .map(ProductWebMapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        Product product = ProductWebMapper.toDomain(request);
        Product created = productUseCase.create(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductWebMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @Valid @RequestBody ProductRequest request) {
        Product product = ProductWebMapper.toDomain(request);
        Product updated = productUseCase.update(id, product);
        return ResponseEntity.status(HttpStatus.OK).body(ProductWebMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productUseCase.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}


