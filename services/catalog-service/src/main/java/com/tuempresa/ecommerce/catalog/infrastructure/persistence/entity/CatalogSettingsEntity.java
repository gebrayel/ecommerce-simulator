package com.tuempresa.ecommerce.catalog.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "catalog_settings")
public class CatalogSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "minimum_stock", nullable = false)
    private Integer minimumStock;

    public CatalogSettingsEntity() {
    }

    public CatalogSettingsEntity(Long id, Integer minimumStock) {
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


