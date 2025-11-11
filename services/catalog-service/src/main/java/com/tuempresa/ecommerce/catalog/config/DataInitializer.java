package com.tuempresa.ecommerce.catalog.config;

import com.tuempresa.ecommerce.catalog.entity.Product;
import com.tuempresa.ecommerce.catalog.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                productRepository.save(new Product("Laptop", "Laptop de alta gama con 16GB RAM", new BigDecimal("1299.99"), 10));
                productRepository.save(new Product("Mouse", "Mouse inalámbrico ergonómico", new BigDecimal("29.99"), 50));
                productRepository.save(new Product("Teclado", "Teclado mecánico RGB", new BigDecimal("89.99"), 30));
                productRepository.save(new Product("Monitor", "Monitor 4K de 27 pulgadas", new BigDecimal("399.99"), 15));
                productRepository.save(new Product("Auriculares", "Auriculares con cancelación de ruido", new BigDecimal("199.99"), 25));
            }
        };
    }
}

