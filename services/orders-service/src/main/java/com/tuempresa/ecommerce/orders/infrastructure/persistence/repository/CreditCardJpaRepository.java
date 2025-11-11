package com.tuempresa.ecommerce.orders.infrastructure.persistence.repository;

import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.CreditCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CreditCardJpaRepository extends JpaRepository<CreditCardEntity, Long> {

    List<CreditCardEntity> findByUserId(Long userId);

    Optional<CreditCardEntity> findByIdAndTokenId(Long id, String tokenId);
}


