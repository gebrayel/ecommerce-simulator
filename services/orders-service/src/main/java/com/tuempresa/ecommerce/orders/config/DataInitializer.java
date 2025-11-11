package com.tuempresa.ecommerce.orders.config;

import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;
import com.tuempresa.ecommerce.orders.domain.port.in.OrderSettingsUseCase;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeOrderSettings(OrderSettingsUseCase orderSettingsUseCase) {
        return args -> {
            OrderSettings settings = orderSettingsUseCase.getSettings();
            boolean needsUpdate = false;

            if (settings.getCardRejectionProbability() == null) {
                settings.setCardRejectionProbability(0.15d);
                needsUpdate = true;
            }

            if (settings.getPaymentRetryAttempts() == null || settings.getPaymentRetryAttempts() < 1) {
                settings.setPaymentRetryAttempts(3);
                needsUpdate = true;
            }

            if (needsUpdate) {
                orderSettingsUseCase.updateSettings(settings);
            }
        };
    }
}


