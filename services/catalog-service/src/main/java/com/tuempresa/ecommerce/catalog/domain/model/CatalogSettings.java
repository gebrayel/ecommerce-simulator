package com.tuempresa.ecommerce.catalog.domain.model;

public class CatalogSettings {

    private Long id;
    private Integer minimumStock;

    public CatalogSettings() {
    }

    public CatalogSettings(Long id, Integer minimumStock) {
        this.id = id;
        this.minimumStock = minimumStock;
    }

    public CatalogSettings(Integer minimumStock) {
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


