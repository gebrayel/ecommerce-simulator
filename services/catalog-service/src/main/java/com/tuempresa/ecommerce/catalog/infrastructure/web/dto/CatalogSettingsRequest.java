package com.tuempresa.ecommerce.catalog.infrastructure.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CatalogSettingsRequest {

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer minimumStock;

    public CatalogSettingsRequest() {
    }

    public CatalogSettingsRequest(Integer minimumStock) {
        this.minimumStock = minimumStock;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock;
    }
}


