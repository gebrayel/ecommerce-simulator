package com.tuempresa.ecommerce.catalog.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class ProductCatalogController {

    @GetMapping("/products-catalog")
    public ResponseEntity<String> getProductsCatalog() {
        return ResponseEntity.status(HttpStatus.OK)
                .body("catalogo completo de productos");
    }
}


