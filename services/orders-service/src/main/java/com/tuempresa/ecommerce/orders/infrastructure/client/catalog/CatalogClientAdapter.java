package com.tuempresa.ecommerce.orders.infrastructure.client.catalog;

import com.tuempresa.ecommerce.orders.domain.model.ProductSnapshot;
import com.tuempresa.ecommerce.orders.domain.port.out.ProductCatalogClientPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class CatalogClientAdapter implements ProductCatalogClientPort {

    private final RestTemplate restTemplate;

    public CatalogClientAdapter(RestTemplateBuilder restTemplateBuilder,
                                @Value("${clients.catalog.base-url}") String baseUrl) {
        this.restTemplate = restTemplateBuilder.rootUri(baseUrl).build();
    }

    @Override
    public Optional<ProductSnapshot> findById(Long productId) {
        try {
            ResponseEntity<ProductResponse> response = restTemplate.getForEntity(
                    "/catalog/products/{id}",
                    ProductResponse.class,
                    productId
            );

            ProductResponse body = response.getBody();
            if (body == null) {
                return Optional.empty();
            }

            return Optional.of(new ProductSnapshot(
                    body.id,
                    body.name,
                    body.description,
                    body.price
            ));
        } catch (HttpClientErrorException.NotFound notFound) {
            return Optional.empty();
        }
    }

    private static class ProductResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}


