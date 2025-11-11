package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.application.service.security.CardTokenService;
import com.tuempresa.ecommerce.orders.application.service.security.CardTokenService.GeneratedToken;
import com.tuempresa.ecommerce.orders.application.service.security.CardTokenService.TokenComponents;
import com.tuempresa.ecommerce.orders.application.service.security.SensitiveDataHasher;
import com.tuempresa.ecommerce.orders.domain.model.CreditCard;
import com.tuempresa.ecommerce.orders.domain.port.out.CreditCardRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditCardServiceTest {

    @Mock
    private CreditCardRepositoryPort creditCardRepositoryPort;
    @Mock
    private SensitiveDataHasher sensitiveDataHasher;
    @Mock
    private CardTokenService cardTokenService;
    @Captor
    private ArgumentCaptor<CreditCard> creditCardCaptor;

    @InjectMocks
    private CreditCardService creditCardService;

    @Test
    void registerCard_hashesDataAndGeneratesToken() {
        when(sensitiveDataHasher.hash("4111111111111111:123")).thenReturn("hashed");
        when(creditCardRepositoryPort.save(any(CreditCard.class))).thenAnswer(invocation -> {
            CreditCard card = invocation.getArgument(0, CreditCard.class);
            if (card.getId() == null) {
                card.setId(1L);
            }
            return card;
        });
        when(cardTokenService.generate(1L, 99L)).thenReturn(new GeneratedToken("token-value", "token-id", "signature"));

        CreditCard result = creditCardService.registerCard(99L, "4111111111111111", "123", 12, YearMonth.now().plusYears(1).getYear());

        verify(sensitiveDataHasher).hash("4111111111111111:123");
        verify(creditCardRepositoryPort, times(2)).save(creditCardCaptor.capture());
        verify(creditCardRepositoryPort, never()).findByUserId(any());
        CreditCard persisted = creditCardCaptor.getAllValues().get(1);
        assertThat(persisted.getId()).isEqualTo(1L);
        assertThat(persisted.getTokenId()).isEqualTo("token-id");
        assertThat(result.getPlainToken()).isEqualTo("token-value");
        assertThat(persisted.getLastFourDigits()).isEqualTo("1111");
    }

    @Test
    void registerCard_throws_whenCardNumberInvalid() {
        assertThatThrownBy(() -> creditCardService.registerCard(1L, "123", "123", 12, YearMonth.now().plusYears(1).getYear()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void registerCard_throws_whenCvvInvalid() {
        assertThatThrownBy(() -> creditCardService.registerCard(1L, "4111111111111111", "1A", 12, YearMonth.now().plusYears(1).getYear()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void registerCard_throws_whenExpiryInvalid() {
        assertThatThrownBy(() -> creditCardService.registerCard(1L, "4111111111111111", "123", 0, YearMonth.now().plusYears(1).getYear()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void registerCard_throws_whenExpiryPast() {
        YearMonth past = YearMonth.now().minusMonths(1);
        assertThatThrownBy(() -> creditCardService.registerCard(1L, "4111111111111111", "123", past.getMonthValue(), past.getYear()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findByUser_delegatesToRepository() {
        when(creditCardRepositoryPort.findByUserId(10L)).thenReturn(List.of(new CreditCard()));

        List<CreditCard> result = creditCardService.findByUser(10L);

        assertThat(result).hasSize(1);
        verify(creditCardRepositoryPort).findByUserId(10L);
    }

    @Test
    void findByToken_returnsEmpty_whenParseFails() {
        when(cardTokenService.parse("invalid")).thenThrow(new IllegalArgumentException("bad token"));

        Optional<CreditCard> result = creditCardService.findByToken("invalid");

        assertThat(result).isEmpty();
    }

    @Test
    void findByToken_returnsCard_whenSignatureValid() {
        CreditCard card = new CreditCard();
        card.setId(3L);
        card.setUserId(9L);
        card.setTokenId("token-id");

        when(cardTokenService.parse("token")).thenReturn(new TokenComponents(3L, "token-id", "signature"));
        when(creditCardRepositoryPort.findByTokenComponents(3L, "token-id")).thenReturn(Optional.of(card));
        when(cardTokenService.isValid(3L, 9L, "token-id", "signature")).thenReturn(true);

        Optional<CreditCard> result = creditCardService.findByToken("token");

        assertThat(result).contains(card);
    }

    @Test
    void findByToken_returnsEmpty_whenSignatureInvalid() {
        CreditCard card = new CreditCard();
        card.setId(3L);
        card.setUserId(9L);
        card.setTokenId("token-id");

        when(cardTokenService.parse("token")).thenReturn(new TokenComponents(3L, "token-id", "signature"));
        when(creditCardRepositoryPort.findByTokenComponents(3L, "token-id")).thenReturn(Optional.of(card));
        when(cardTokenService.isValid(3L, 9L, "token-id", "signature")).thenReturn(false);

        Optional<CreditCard> result = creditCardService.findByToken("token");

        assertThat(result).isEmpty();
    }
}

