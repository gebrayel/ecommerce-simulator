package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderSettingsRepositoryPort;
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
class OrderSettingsServiceTest {

    @Mock
    private OrderSettingsRepositoryPort orderSettingsRepositoryPort;

    @InjectMocks
    private OrderSettingsService orderSettingsService;

    @Test
    void getSettings_returnsDefault_whenMissing() {
        when(orderSettingsRepositoryPort.findSettings()).thenReturn(Optional.empty());

        OrderSettings settings = orderSettingsService.getSettings();

        assertThat(settings.getCardRejectionProbability()).isEqualTo(0.0d);
        assertThat(settings.getPaymentRetryAttempts()).isEqualTo(1);
    }

    @Test
    void updateSettings_clampsValuesWithinRange() {
        OrderSettings current = new OrderSettings(null, 0.2d, 1);
        when(orderSettingsRepositoryPort.findSettings()).thenReturn(Optional.of(current));
        when(orderSettingsRepositoryPort.save(any(OrderSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderSettings incoming = new OrderSettings(null, 2.0d, -1);
        OrderSettings saved = orderSettingsService.updateSettings(incoming);

        assertThat(saved.getCardRejectionProbability()).isEqualTo(1.0d);
        assertThat(saved.getPaymentRetryAttempts()).isEqualTo(1);
        verify(orderSettingsRepositoryPort).save(current);
    }
}

