package com.tuempresa.ecommerce.catalog.infrastructure.web.mapper;

import com.tuempresa.ecommerce.catalog.domain.model.CatalogSettings;
import com.tuempresa.ecommerce.catalog.infrastructure.web.dto.CatalogSettingsRequest;
import com.tuempresa.ecommerce.catalog.infrastructure.web.dto.CatalogSettingsResponse;

public class CatalogSettingsWebMapper {

    private CatalogSettingsWebMapper() {
    }

    public static CatalogSettingsResponse toResponse(CatalogSettings settings) {
        if (settings == null) {
            return null;
        }
        return new CatalogSettingsResponse(
                settings.getId(),
                settings.getMinimumStock()
        );
    }

    public static CatalogSettings toDomain(CatalogSettingsRequest request, Long id) {
        if (request == null) {
            return null;
        }
        CatalogSettings settings = new CatalogSettings();
        settings.setId(id);
        settings.setMinimumStock(request.getMinimumStock());
        return settings;
    }
}


