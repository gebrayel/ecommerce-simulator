package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.exception.InvalidOrderStateException;
import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.model.Cart;
import com.tuempresa.ecommerce.orders.domain.model.CartItem;
import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.model.OrderItem;
import com.tuempresa.ecommerce.orders.domain.port.out.CartRepositoryPort;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;
    @Mock
    private CartRepositoryPort cartRepositoryPort;
    @Captor
    private ArgumentCaptor<Order> orderCaptor;
    @Captor
    private ArgumentCaptor<Cart> cartCaptor;

    @InjectMocks
    private OrderService orderService;

    private Cart cart;

    @BeforeEach
    void setUp() {
        CartItem item = new CartItem(5L, "Mouse", "Gaming", 2, BigDecimal.valueOf(25));
        cart = new Cart();
        cart.setId(10L);
        cart.setUserId(1L);
        cart.setItems(List.of(item));
    }

    @Test
    void createFromCart_transformsCartIntoOrder_andClearsCart() {
        when(cartRepositoryPort.findById(10L)).thenReturn(Optional.of(cart));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0, Order.class);
            saved.setId(99L);
            return saved;
        });

        Order result = orderService.createFromCart(10L, "  Street 123  ");

        verify(orderRepositoryPort).save(orderCaptor.capture());
        Order persistedOrder = orderCaptor.getValue();
        assertThat(persistedOrder.getUserId()).isEqualTo(1L);
        assertThat(persistedOrder.getDeliveryAddress()).isEqualTo("Street 123");
        assertThat(persistedOrder.getItems()).hasSize(1);
        OrderItem orderItem = persistedOrder.getItems().get(0);
        assertThat(orderItem.getProductId()).isEqualTo(5L);
        assertThat(orderItem.getQuantity()).isEqualTo(2);
        assertThat(persistedOrder.getTotal()).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(result.getId()).isEqualTo(99L);

        verify(cartRepositoryPort).save(cartCaptor.capture());
        Cart clearedCart = cartCaptor.getValue();
        assertThat(clearedCart.getItems()).isEmpty();
    }

    @Test
    void createFromCart_throws_whenAddressMissing() {
        assertThatThrownBy(() -> orderService.createFromCart(10L, "  "))
                .isInstanceOf(IllegalArgumentException.class);

        verify(cartRepositoryPort, never()).findById(any());
    }

    @Test
    void createFromCart_throws_whenCartEmpty() {
        Cart emptyCart = new Cart();
        emptyCart.setId(10L);
        emptyCart.setItems(List.of());
        when(cartRepositoryPort.findById(10L)).thenReturn(Optional.of(emptyCart));

        assertThatThrownBy(() -> orderService.createFromCart(10L, "Street"))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void createFromCart_throws_whenCartNotFound() {
        when(cartRepositoryPort.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createFromCart(10L, "Street"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findById_returnsOrder_whenExists() {
        Order order = new Order();
        order.setId(50L);
        when(orderRepositoryPort.findById(50L)).thenReturn(Optional.of(order));

        Order result = orderService.findById(50L);

        assertThat(result).isSameAs(order);
    }

    @Test
    void markAsPaid_updatesOrder_whenValid() {
        Order order = new Order();
        order.setId(55L);
        order.setStatus(Order.Status.CREATED);

        when(orderRepositoryPort.findById(55L)).thenReturn(Optional.of(order));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order updated = orderService.markAsPaid(55L);

        assertThat(updated.getStatus()).isEqualTo(Order.Status.PAID);
        verify(orderRepositoryPort).save(order);
    }

    @Test
    void markAsPaid_throws_whenCancelled() {
        Order order = new Order();
        order.setStatus(Order.Status.CANCELLED);
        when(orderRepositoryPort.findById(55L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.markAsPaid(55L))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void cancel_updatesOrder_whenValid() {
        Order order = new Order();
        order.setId(77L);
        order.setStatus(Order.Status.CREATED);

        when(orderRepositoryPort.findById(77L)).thenReturn(Optional.of(order));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order cancelled = orderService.cancel(77L);

        assertThat(cancelled.getStatus()).isEqualTo(Order.Status.CANCELLED);
        verify(orderRepositoryPort).save(order);
    }

    @Test
    void cancel_throws_whenAlreadyPaidOrCompleted() {
        for (Order.Status status : List.of(Order.Status.PAID, Order.Status.COMPLETED)) {
            Order order = new Order();
            order.setStatus(status);
            when(orderRepositoryPort.findById(100L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancel(100L))
                    .isInstanceOf(InvalidOrderStateException.class);
        }
    }
}

