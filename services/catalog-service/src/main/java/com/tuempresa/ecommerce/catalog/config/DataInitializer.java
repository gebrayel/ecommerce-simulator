package com.tuempresa.ecommerce.catalog.config;

import com.tuempresa.ecommerce.catalog.domain.model.CatalogSettings;
import com.tuempresa.ecommerce.catalog.domain.model.Product;
import com.tuempresa.ecommerce.catalog.domain.port.in.CatalogSettingsUseCase;
import com.tuempresa.ecommerce.catalog.domain.port.in.ProductUseCase;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(ProductUseCase productUseCase, CatalogSettingsUseCase catalogSettingsUseCase) {
        return args -> {
            if (productUseCase.findAll(0, 1, null).getTotalElements() == 0) {
                productUseCase.create(new Product("Laptop", "Laptop de alta gama con 16GB RAM", new BigDecimal("1299.99"), 10));
                productUseCase.create(new Product("Mouse", "Mouse inalámbrico ergonómico", new BigDecimal("29.99"), 50));
                productUseCase.create(new Product("Teclado", "Teclado mecánico RGB", new BigDecimal("89.99"), 30));
                productUseCase.create(new Product("Monitor", "Monitor 4K de 27 pulgadas", new BigDecimal("399.99"), 15));
                productUseCase.create(new Product("Auriculares", "Auriculares con cancelación de ruido", new BigDecimal("199.99"), 25));
            }

            CatalogSettings settings = catalogSettingsUseCase.getSettings();
            if (settings.getMinimumStock() == null) {
                settings.setMinimumStock(0);
                catalogSettingsUseCase.updateSettings(settings);
            }
        };
    }
}

