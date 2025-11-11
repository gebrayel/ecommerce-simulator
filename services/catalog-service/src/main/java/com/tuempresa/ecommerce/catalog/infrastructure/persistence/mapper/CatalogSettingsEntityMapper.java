package com.tuempresa.ecommerce.catalog.infrastructure.persistence.mapper;

import com.tuempresa.ecommerce.catalog.domain.model.CatalogSettings;
import com.tuempresa.ecommerce.catalog.infrastructure.persistence.entity.CatalogSettingsEntity;

public class CatalogSettingsEntityMapper {

    private CatalogSettingsEntityMapper() {
    }

    public static CatalogSettings toDomain(CatalogSettingsEntity entity) {
        if (entity == null) {
            return null;
        }
        return new CatalogSettings(
                entity.getId(),
                entity.getMinimumStock()
        );
    }

    public static CatalogSettingsEntity toEntity(CatalogSettings settings) {
        if (settings == null) {
            return null;
        }
        return new CatalogSettingsEntity(
                settings.getId(),
                settings.getMinimumStock()
        );
    }
}


