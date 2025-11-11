package com.tuempresa.ecommerce.orders.infrastructure.web.mapper;

import com.tuempresa.ecommerce.orders.domain.model.CreditCard;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreditCardResponse;

public class CreditCardWebMapper {

    private CreditCardWebMapper() {
    }

    public static CreditCardResponse toResponse(CreditCard card) {
        return new CreditCardResponse(
                card.getId(),
                resolveToken(card),
                card.getLastFourDigits(),
                card.getExpiryMonth(),
                card.getExpiryYear(),
                card.getCreatedAt()
        );
    }

    private static String resolveToken(CreditCard card) {
        if (card.getPlainToken() != null) {
            return card.getPlainToken();
        }
        if (card.getId() == null || card.getTokenId() == null || card.getTokenSignature() == null) {
            return null;
        }
        return card.getId() + "." + card.getTokenId() + "." + card.getTokenSignature();
    }
}


