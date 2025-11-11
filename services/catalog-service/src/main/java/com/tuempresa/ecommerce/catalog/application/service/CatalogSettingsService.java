package com.tuempresa.ecommerce.catalog.application.service;

import com.tuempresa.ecommerce.catalog.domain.model.CatalogSettings;
import com.tuempresa.ecommerce.catalog.domain.port.in.CatalogSettingsUseCase;
import com.tuempresa.ecommerce.catalog.domain.port.out.CatalogSettingsRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CatalogSettingsService implements CatalogSettingsUseCase {

    private final CatalogSettingsRepositoryPort catalogSettingsRepositoryPort;

    public CatalogSettingsService(CatalogSettingsRepositoryPort catalogSettingsRepositoryPort) {
        this.catalogSettingsRepositoryPort = catalogSettingsRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogSettings getSettings() {
        return catalogSettingsRepositoryPort.findSettings()
                .orElseGet(() -> new CatalogSettings(null, 0));
    }

    @Override
    public CatalogSettings updateSettings(CatalogSettings settings) {
        if (settings.getMinimumStock() == null) {
            settings.setMinimumStock(0);
        }
        return catalogSettingsRepositoryPort.save(settings);
    }
}


