package com.tuempresa.ecommerce.libs.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductDtoTest {

    @Test
    void constructor_setsFields() {
        ProductDto dto = new ProductDto(1L, "Laptop", 999.0);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Laptop");
        assertThat(dto.getPrice()).isEqualTo(999.0);
    }

    @Test
    void setters_updateValues() {
        ProductDto dto = new ProductDto();

        dto.setId(2L);
        dto.setName("Phone");
        dto.setPrice(499.0);

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getName()).isEqualTo("Phone");
        assertThat(dto.getPrice()).isEqualTo(499.0);
    }
}

