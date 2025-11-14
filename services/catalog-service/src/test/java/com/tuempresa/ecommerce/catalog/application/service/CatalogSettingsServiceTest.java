package com.tuempresa.ecommerce.catalog.application.service;

import com.tuempresa.ecommerce.catalog.domain.model.CatalogSettings;
import com.tuempresa.ecommerce.catalog.domain.port.out.CatalogSettingsRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogSettingsService Tests")
class CatalogSettingsServiceTest {

    @Mock
    private CatalogSettingsRepositoryPort catalogSettingsRepositoryPort;

    @InjectMocks
    private CatalogSettingsService catalogSettingsService;

    private CatalogSettings testSettings;

    @BeforeEach
    void setUp() {
        testSettings = new CatalogSettings(1L, 10);
    }

    @Test
    @DisplayName("Should return settings when they exist")
    void shouldReturnSettingsWhenTheyExist() {
        // Given
        when(catalogSettingsRepositoryPort.findSettings()).thenReturn(Optional.of(testSettings));

        // When
        CatalogSettings result = catalogSettingsService.getSettings();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMinimumStock()).isEqualTo(10);
        verify(catalogSettingsRepositoryPort, times(1)).findSettings();
    }

    @Test
    @DisplayName("Should return default settings when they don't exist")
    void shouldReturnDefaultSettingsWhenTheyDontExist() {
        // Given
        when(catalogSettingsRepositoryPort.findSettings()).thenReturn(Optional.empty());

        // When
        CatalogSettings result = catalogSettingsService.getSettings();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMinimumStock()).isEqualTo(0);
        verify(catalogSettingsRepositoryPort, times(1)).findSettings();
    }

    @Test
    @DisplayName("Should update settings successfully")
    void shouldUpdateSettingsSuccessfully() {
        // Given
        CatalogSettings updatedSettings = new CatalogSettings(1L, 20);
        when(catalogSettingsRepositoryPort.save(any(CatalogSettings.class))).thenReturn(updatedSettings);

        // When
        CatalogSettings result = catalogSettingsService.updateSettings(updatedSettings);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMinimumStock()).isEqualTo(20);
        verify(catalogSettingsRepositoryPort, times(1)).save(any(CatalogSettings.class));
    }

    @Test
    @DisplayName("Should set minimumStock to 0 when null")
    void shouldSetMinimumStockToZeroWhenNull() {
        // Given
        CatalogSettings settingsWithNull = new CatalogSettings(1L, null);
        when(catalogSettingsRepositoryPort.save(any(CatalogSettings.class))).thenAnswer(invocation -> {
            CatalogSettings settings = invocation.getArgument(0);
            assertThat(settings.getMinimumStock()).isEqualTo(0);
            return settings;
        });

        // When
        catalogSettingsService.updateSettings(settingsWithNull);

        // Then
        verify(catalogSettingsRepositoryPort, times(1)).save(any(CatalogSettings.class));
    }
}

