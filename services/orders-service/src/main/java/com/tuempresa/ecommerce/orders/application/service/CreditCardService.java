package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.application.service.security.CardTokenService;
import com.tuempresa.ecommerce.orders.application.service.security.CardTokenService.GeneratedToken;
import com.tuempresa.ecommerce.orders.application.service.security.CardTokenService.TokenComponents;
import com.tuempresa.ecommerce.orders.application.service.security.SensitiveDataHasher;
import com.tuempresa.ecommerce.orders.domain.model.CreditCard;
import com.tuempresa.ecommerce.orders.domain.port.in.CreditCardUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.CreditCardRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CreditCardService implements CreditCardUseCase {

    private final CreditCardRepositoryPort repositoryPort;
    private final SensitiveDataHasher hasher;
    private final CardTokenService cardTokenService;

    public CreditCardService(CreditCardRepositoryPort repositoryPort,
                             SensitiveDataHasher hasher,
                             CardTokenService cardTokenService) {
        this.repositoryPort = repositoryPort;
        this.hasher = hasher;
        this.cardTokenService = cardTokenService;
    }

    @Override
    public CreditCard registerCard(Long userId, String cardNumber, String cvv, Integer expiryMonth, Integer expiryYear) {
        validateCardData(cardNumber, cvv, expiryMonth, expiryYear);
        String cardHash = hasher.hash(cardNumber + ":" + cvv);
        String lastFour = extractLastFour(cardNumber);

        CreditCard card = new CreditCard(userId, cardHash, lastFour, expiryMonth, expiryYear);
        CreditCard saved = repositoryPort.save(card);

        GeneratedToken generatedToken = cardTokenService.generate(saved.getId(), saved.getUserId());
        saved.setTokenId(generatedToken.tokenId());
        saved.setTokenSignature(generatedToken.signature());
        saved.setPlainToken(generatedToken.token());

        CreditCard updated = repositoryPort.save(saved);
        updated.setPlainToken(generatedToken.token());
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditCard> findByUser(Long userId) {
        return repositoryPort.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CreditCard> findByToken(String token) {
        TokenComponents components;
        try {
            components = cardTokenService.parse(token);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
        return repositoryPort.findByTokenComponents(components.cardId(), components.tokenId())
                .filter(card -> cardTokenService.isValid(
                        card.getId(),
                        card.getUserId(),
                        card.getTokenId(),
                        components.signature()
                ));
    }

    private void validateCardData(String cardNumber, String cvv, Integer expiryMonth, Integer expiryYear) {
        if (cardNumber == null || cardNumber.length() < 12 || cardNumber.length() > 19) {
            throw new IllegalArgumentException("Número de tarjeta inválido");
        }
        if (!cardNumber.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("El número de tarjeta debe ser numérico");
        }
        if (cvv == null || cvv.length() < 3 || cvv.length() > 4 || !cvv.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("CVV inválido");
        }
        if (expiryMonth == null || expiryMonth < 1 || expiryMonth > 12) {
            throw new IllegalArgumentException("Mes de expiración inválido");
        }
        if (expiryYear == null || expiryYear < YearMonth.now().getYear()) {
            throw new IllegalArgumentException("Año de expiración inválido");
        }
        YearMonth expiry = YearMonth.of(expiryYear, expiryMonth);
        if (expiry.isBefore(YearMonth.now())) {
            throw new IllegalArgumentException("La tarjeta se encuentra vencida");
        }
    }

    private String extractLastFour(String cardNumber) {
        if (cardNumber.length() <= 4) {
            return cardNumber;
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }
}


