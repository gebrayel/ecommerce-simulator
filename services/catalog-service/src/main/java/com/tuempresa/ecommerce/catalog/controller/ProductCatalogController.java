package com.tuempresa.ecommerce.catalog.controller;

import com.tuempresa.ecommerce.catalog.entity.Product;
import com.tuempresa.ecommerce.catalog.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class ProductCatalogController {

    private final ProductRepository productRepository;

    public ProductCatalogController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/products-catalog")
    public ResponseEntity<List<Product>> getProductsCatalog() {
        List<Product> products = productRepository.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(products);
    }
}


