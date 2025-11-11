package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.exception.UnauthorizedAccessException;
import com.tuempresa.ecommerce.orders.domain.model.CreditCard;
import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;
import com.tuempresa.ecommerce.orders.domain.model.Payment;
import com.tuempresa.ecommerce.orders.domain.port.in.CreditCardUseCase;
import com.tuempresa.ecommerce.orders.domain.port.in.OrderSettingsUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderRepositoryPort;
import com.tuempresa.ecommerce.orders.domain.port.out.PaymentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepositoryPort paymentRepositoryPort;
    @Mock
    private OrderRepositoryPort orderRepositoryPort;
    @Mock
    private CreditCardUseCase creditCardUseCase;
    @Mock
    private OrderSettingsUseCase orderSettingsUseCase;
    @InjectMocks
    private PaymentService paymentService;

    private Order order;
    private CreditCard creditCard;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(10L);
        order.setUserId(15L);

        creditCard = new CreditCard();
        creditCard.setId(7L);
        creditCard.setUserId(15L);
        creditCard.setLastFourDigits("1234");
        creditCard.setTokenId("token-id");
        creditCard.setExpiryMonth(YearMonth.now().plusMonths(2).getMonthValue());
        creditCard.setExpiryYear(YearMonth.now().plusMonths(2).getYear());
    }

    @Test
    void registerPayment_marksPaymentAndOrderCompleted_whenApproved() {
        when(orderRepositoryPort.findById(10L)).thenReturn(Optional.of(order));
        when(creditCardUseCase.findByToken("secure-token")).thenReturn(Optional.of(creditCard));
        when(orderSettingsUseCase.getSettings()).thenReturn(new OrderSettings(0.0d, 3));
        when(paymentRepositoryPort.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0, Payment.class));

        Payment payment = paymentService.registerPayment(
                15L,
                10L,
                BigDecimal.valueOf(100),
                "CARD",
                "secure-token"
        );

        assertThat(payment.getStatus()).isEqualTo(Payment.Status.COMPLETED);
        assertThat(payment.getAttempts()).isEqualTo(1);
        verify(orderRepositoryPort).save(order);
        assertThat(order.getStatus()).isEqualTo(Order.Status.PAID);
    }

    @Test
    void registerPayment_marksPaymentFailed_whenAllAttemptsRejected() {
        when(orderRepositoryPort.findById(10L)).thenReturn(Optional.of(order));
        when(creditCardUseCase.findByToken("secure-token")).thenReturn(Optional.of(creditCard));
        when(orderSettingsUseCase.getSettings()).thenReturn(new OrderSettings(1.0d, 2));
        when(paymentRepositoryPort.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0, Payment.class));

        Payment payment = paymentService.registerPayment(
                15L,
                10L,
                BigDecimal.valueOf(100),
                "CARD",
                "secure-token"
        );

        assertThat(payment.getStatus()).isEqualTo(Payment.Status.FAILED);
        assertThat(payment.getAttempts()).isEqualTo(2);
        verify(orderRepositoryPort, never()).save(order);
        assertThat(order.getStatus()).isEqualTo(Order.Status.CREATED);
    }

    @Test
    void registerPayment_throwsUnauthorized_whenOrderBelongsToAnotherUser() {
        order.setUserId(20L);
        when(orderRepositoryPort.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.registerPayment(
                15L,
                10L,
                BigDecimal.TEN,
                "CARD",
                "secure-token"
        )).isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void registerPayment_throwsUnauthorized_whenCardTokenInvalid() {
        when(orderRepositoryPort.findById(10L)).thenReturn(Optional.of(order));
        when(creditCardUseCase.findByToken("secure-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.registerPayment(
                15L,
                10L,
                BigDecimal.TEN,
                "CARD",
                "secure-token"
        )).isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void registerPayment_throwsUnauthorized_whenCardBelongsToAnotherUser() {
        creditCard.setUserId(999L);
        when(orderRepositoryPort.findById(10L)).thenReturn(Optional.of(order));
        when(creditCardUseCase.findByToken("secure-token")).thenReturn(Optional.of(creditCard));

        assertThatThrownBy(() -> paymentService.registerPayment(
                15L,
                10L,
                BigDecimal.TEN,
                "CARD",
                "secure-token"
        )).isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void registerPayment_throwsUnauthorized_whenCardExpired() {
        creditCard.setExpiryMonth(YearMonth.now().minusMonths(1).getMonthValue());
        creditCard.setExpiryYear(YearMonth.now().minusMonths(1).getYear());
        when(orderRepositoryPort.findById(10L)).thenReturn(Optional.of(order));
        when(creditCardUseCase.findByToken("secure-token")).thenReturn(Optional.of(creditCard));

        assertThatThrownBy(() -> paymentService.registerPayment(
                15L, 10L, BigDecimal.TEN, "CARD", "secure-token"
        )).isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void markAsCompleted_updatesPaymentAndOrder() {
        Payment payment = new Payment();
        payment.setId(30L);
        payment.setOrderId(10L);
        payment.setStatus(Payment.Status.PENDING);
        when(paymentRepositoryPort.findById(30L)).thenReturn(Optional.of(payment));
        when(paymentRepositoryPort.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0, Payment.class));
        when(orderRepositoryPort.findById(10L)).thenReturn(Optional.of(order));

        Payment result = paymentService.markAsCompleted(30L);

        assertThat(result.getStatus()).isEqualTo(Payment.Status.COMPLETED);
        verify(orderRepositoryPort).save(order);
        assertThat(order.getStatus()).isEqualTo(Order.Status.PAID);
    }

    @Test
    void markAsFailed_updatesPaymentStatus() {
        Payment payment = new Payment();
        payment.setId(30L);
        when(paymentRepositoryPort.findById(30L)).thenReturn(Optional.of(payment));
        when(paymentRepositoryPort.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0, Payment.class));

        Payment result = paymentService.markAsFailed(30L);

        assertThat(result.getStatus()).isEqualTo(Payment.Status.FAILED);
    }

    @Test
    void findById_returnsPayment_whenExists() {
        Payment payment = new Payment();
        payment.setId(30L);
        when(paymentRepositoryPort.findById(30L)).thenReturn(Optional.of(payment));

        Payment result = paymentService.findById(30L);

        assertThat(result).isSameAs(payment);
    }

    @Test
    void findById_throwsResourceNotFound_whenMissing() {
        when(paymentRepositoryPort.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

