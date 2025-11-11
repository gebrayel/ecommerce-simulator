package com.tuempresa.ecommerce.orders.infrastructure.persistence.adapter;

import com.tuempresa.ecommerce.orders.domain.model.CreditCard;
import com.tuempresa.ecommerce.orders.domain.port.out.CreditCardRepositoryPort;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.CreditCardEntity;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper.CreditCardMapper;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.repository.CreditCardJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CreditCardRepositoryAdapter implements CreditCardRepositoryPort {

    private final CreditCardJpaRepository repository;

    public CreditCardRepositoryAdapter(CreditCardJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public CreditCard save(CreditCard creditCard) {
        CreditCardEntity entity = CreditCardMapper.toEntity(creditCard);
        CreditCardEntity saved = repository.save(entity);
        return CreditCardMapper.toDomain(saved);
    }

    @Override
    public Optional<CreditCard> findById(Long id) {
        return repository.findById(id).map(CreditCardMapper::toDomain);
    }

    @Override
    public List<CreditCard> findByUserId(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(CreditCardMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<CreditCard> findByTokenComponents(Long cardId, String tokenId) {
        return repository.findByIdAndTokenId(cardId, tokenId)
                .map(CreditCardMapper::toDomain);
    }
}


