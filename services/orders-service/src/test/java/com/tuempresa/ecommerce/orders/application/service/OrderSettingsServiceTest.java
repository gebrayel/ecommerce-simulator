package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderSettingsRepositoryPort;
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
@DisplayName("OrderSettingsService Tests")
class OrderSettingsServiceTest {

    @Mock
    private OrderSettingsRepositoryPort repositoryPort;

    @InjectMocks
    private OrderSettingsService orderSettingsService;

    private OrderSettings testSettings;

    @BeforeEach
    void setUp() {
        testSettings = new OrderSettings(1L, 0.1, 3);
    }

    @Test
    @DisplayName("Should return settings when they exist")
    void shouldReturnSettingsWhenTheyExist() {
        // Given
        when(repositoryPort.findSettings()).thenReturn(Optional.of(testSettings));

        // When
        OrderSettings result = orderSettingsService.getSettings();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCardRejectionProbability()).isEqualTo(0.1);
        assertThat(result.getPaymentRetryAttempts()).isEqualTo(3);
        verify(repositoryPort, times(1)).findSettings();
    }

    @Test
    @DisplayName("Should return default settings when they don't exist")
    void shouldReturnDefaultSettingsWhenTheyDontExist() {
        // Given
        when(repositoryPort.findSettings()).thenReturn(Optional.empty());

        // When
        OrderSettings result = orderSettingsService.getSettings();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCardRejectionProbability()).isEqualTo(0.0);
        assertThat(result.getPaymentRetryAttempts()).isEqualTo(1);
        verify(repositoryPort, times(1)).findSettings();
    }

    @Test
    @DisplayName("Should update settings successfully")
    void shouldUpdateSettingsSuccessfully() {
        // Given
        OrderSettings updatedSettings = new OrderSettings(1L, 0.2, 5);
        when(repositoryPort.findSettings()).thenReturn(Optional.of(testSettings));
        when(repositoryPort.save(any(OrderSettings.class))).thenReturn(updatedSettings);

        // When
        OrderSettings result = orderSettingsService.updateSettings(updatedSettings);

        // Then
        assertThat(result).isNotNull();
        verify(repositoryPort, times(1)).findSettings();
        verify(repositoryPort, times(1)).save(any(OrderSettings.class));
    }

    @Test
    @DisplayName("Should clamp rejection probability to 0 when negative")
    void shouldClampRejectionProbabilityToZeroWhenNegative() {
        // Given
        OrderSettings settingsWithNegative = new OrderSettings(1L, -0.1, 3);
        when(repositoryPort.findSettings()).thenReturn(Optional.of(testSettings));
        when(repositoryPort.save(any(OrderSettings.class))).thenAnswer(invocation -> {
            OrderSettings settings = invocation.getArgument(0);
            assertThat(settings.getCardRejectionProbability()).isEqualTo(0.0);
            return settings;
        });

        // When
        orderSettingsService.updateSettings(settingsWithNegative);

        // Then
        verify(repositoryPort, times(1)).save(any(OrderSettings.class));
    }

    @Test
    @DisplayName("Should clamp rejection probability to 1 when greater than 1")
    void shouldClampRejectionProbabilityToOneWhenGreaterThanOne() {
        // Given
        OrderSettings settingsWithHigh = new OrderSettings(1L, 1.5, 3);
        when(repositoryPort.findSettings()).thenReturn(Optional.of(testSettings));
        when(repositoryPort.save(any(OrderSettings.class))).thenAnswer(invocation -> {
            OrderSettings settings = invocation.getArgument(0);
            assertThat(settings.getCardRejectionProbability()).isEqualTo(1.0);
            return settings;
        });

        // When
        orderSettingsService.updateSettings(settingsWithHigh);

        // Then
        verify(repositoryPort, times(1)).save(any(OrderSettings.class));
    }

    @Test
    @DisplayName("Should set payment retry attempts to 1 when null or less than 1")
    void shouldSetPaymentRetryAttemptsToOneWhenInvalid() {
        // Given
        OrderSettings settingsWithInvalid = new OrderSettings(1L, 0.1, 0);
        when(repositoryPort.findSettings()).thenReturn(Optional.of(testSettings));
        when(repositoryPort.save(any(OrderSettings.class))).thenAnswer(invocation -> {
            OrderSettings settings = invocation.getArgument(0);
            assertThat(settings.getPaymentRetryAttempts()).isEqualTo(1);
            return settings;
        });

        // When
        orderSettingsService.updateSettings(settingsWithInvalid);

        // Then
        verify(repositoryPort, times(1)).save(any(OrderSettings.class));
    }
}

