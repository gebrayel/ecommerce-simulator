package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;
import com.tuempresa.ecommerce.orders.domain.port.in.OrderSettingsUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderSettingsRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderSettingsService implements OrderSettingsUseCase {

    private final OrderSettingsRepositoryPort repositoryPort;

    public OrderSettingsService(OrderSettingsRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderSettings getSettings() {
        return repositoryPort.findSettings()
                .orElseGet(() -> new OrderSettings(null, 0.0d, 1));
    }

    @Override
    public OrderSettings updateSettings(OrderSettings settings) {
        OrderSettings current = repositoryPort.findSettings()
                .orElseGet(() -> new OrderSettings(null, 0.0d, 1));

        if (settings.getCardRejectionProbability() == null || settings.getCardRejectionProbability() < 0) {
            current.setCardRejectionProbability(0.0d);
        } else if (settings.getCardRejectionProbability() > 1) {
            current.setCardRejectionProbability(1.0d);
        } else {
            current.setCardRejectionProbability(settings.getCardRejectionProbability());
        }

        if (settings.getPaymentRetryAttempts() == null || settings.getPaymentRetryAttempts() < 1) {
            current.setPaymentRetryAttempts(1);
        } else {
            current.setPaymentRetryAttempts(settings.getPaymentRetryAttempts());
        }

        return repositoryPort.save(current);
    }
}


