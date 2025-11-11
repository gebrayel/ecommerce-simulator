package com.tuempresa.ecommerce.catalog.infrastructure.web.dto;

public class CatalogSettingsResponse {

    private Long id;
    private Integer minimumStock;

    public CatalogSettingsResponse() {
    }

    public CatalogSettingsResponse(Long id, Integer minimumStock) {
        this.id = id;
        this.minimumStock = minimumStock;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock;
    }
}


