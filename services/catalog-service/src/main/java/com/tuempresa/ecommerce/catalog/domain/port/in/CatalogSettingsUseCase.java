package com.tuempresa.ecommerce.catalog.domain.port.in;

import com.tuempresa.ecommerce.catalog.domain.model.CatalogSettings;

public interface CatalogSettingsUseCase {

    CatalogSettings getSettings();

    CatalogSettings updateSettings(CatalogSettings settings);
}


