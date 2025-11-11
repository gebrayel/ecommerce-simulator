package com.tuempresa.ecommerce.orders.domain.port.out;

import com.tuempresa.ecommerce.orders.domain.model.CreditCard;

import java.util.List;
import java.util.Optional;

public interface CreditCardRepositoryPort {

    CreditCard save(CreditCard creditCard);

    Optional<CreditCard> findById(Long id);

    List<CreditCard> findByUserId(Long userId);

    Optional<CreditCard> findByTokenComponents(Long cardId, String tokenId);
}


