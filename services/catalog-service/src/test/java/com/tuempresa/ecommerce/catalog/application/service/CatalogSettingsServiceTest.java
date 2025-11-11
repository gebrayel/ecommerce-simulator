package com.tuempresa.ecommerce.catalog.application.service;

import com.tuempresa.ecommerce.catalog.domain.model.CatalogSettings;
import com.tuempresa.ecommerce.catalog.domain.port.out.CatalogSettingsRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogSettingsServiceTest {

    @Mock
    private CatalogSettingsRepositoryPort catalogSettingsRepositoryPort;

    @InjectMocks
    private CatalogSettingsService catalogSettingsService;

    @Test
    void getSettings_returnsDefault_whenRepositoryEmpty() {
        when(catalogSettingsRepositoryPort.findSettings()).thenReturn(Optional.empty());

        CatalogSettings result = catalogSettingsService.getSettings();

        assertThat(result.getId()).isNull();
        assertThat(result.getMinimumStock()).isZero();
    }

    @Test
    void updateSettings_normalizesNullMinimumStock_toZero() {
        CatalogSettings input = new CatalogSettings(1L, null);
        when(catalogSettingsRepositoryPort.save(any(CatalogSettings.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, CatalogSettings.class));

        CatalogSettings updated = catalogSettingsService.updateSettings(input);

        assertThat(updated.getMinimumStock()).isZero();
        verify(catalogSettingsRepositoryPort).save(input);
    }
}

