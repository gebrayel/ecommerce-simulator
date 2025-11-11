package com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper;

import com.tuempresa.ecommerce.orders.domain.model.CreditCard;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.CreditCardEntity;

public class CreditCardMapper {

    private CreditCardMapper() {
    }

    public static CreditCardEntity toEntity(CreditCard creditCard) {
        CreditCardEntity entity = new CreditCardEntity();
        entity.setId(creditCard.getId());
        entity.setUserId(creditCard.getUserId());
        entity.setCardNumberHash(creditCard.getCardNumberHash());
        entity.setLastFourDigits(creditCard.getLastFourDigits());
        entity.setExpiryMonth(creditCard.getExpiryMonth());
        entity.setExpiryYear(creditCard.getExpiryYear());
        entity.setTokenId(creditCard.getTokenId());
        entity.setTokenSignature(creditCard.getTokenSignature());
        entity.setCreatedAt(creditCard.getCreatedAt());
        return entity;
    }

    public static CreditCard toDomain(CreditCardEntity entity) {
        return new CreditCard(
                entity.getId(),
                entity.getUserId(),
                entity.getCardNumberHash(),
                entity.getLastFourDigits(),
                entity.getExpiryMonth(),
                entity.getExpiryYear(),
                entity.getTokenId(),
                entity.getTokenSignature(),
                entity.getCreatedAt()
        );
    }
}


