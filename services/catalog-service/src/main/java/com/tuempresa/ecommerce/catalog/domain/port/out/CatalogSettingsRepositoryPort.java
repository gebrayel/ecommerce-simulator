package com.tuempresa.ecommerce.catalog.domain.port.out;

import com.tuempresa.ecommerce.catalog.domain.model.CatalogSettings;

import java.util.Optional;

public interface CatalogSettingsRepositoryPort {

    Optional<CatalogSettings> findSettings();

    CatalogSettings save(CatalogSettings settings);
}


