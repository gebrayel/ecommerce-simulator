package com.tuempresa.ecommerce.orders.domain.port.in;

import com.tuempresa.ecommerce.orders.domain.model.CreditCard;

import java.util.List;
import java.util.Optional;

public interface CreditCardUseCase {

    CreditCard registerCard(Long userId, String cardNumber, String cvv, Integer expiryMonth, Integer expiryYear);

    List<CreditCard> findByUser(Long userId);

    Optional<CreditCard> findByToken(String token);
}


