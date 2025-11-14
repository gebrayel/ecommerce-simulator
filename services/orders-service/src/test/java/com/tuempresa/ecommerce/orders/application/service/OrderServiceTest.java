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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private CartRepositoryPort cartRepositoryPort;

    @InjectMocks
    private OrderService orderService;

    private Cart testCart;
    private Order testOrder;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        cartItem = new CartItem(1L, "Test Product", "Description", 2, BigDecimal.valueOf(19.99));
        List<CartItem> items = new ArrayList<>(Arrays.asList(cartItem));
        testCart = new Cart(1L, 1L, "user@example.com", "Test User", "+1234567890", 
                           "Test Address", items, BigDecimal.valueOf(39.98), 
                           LocalDateTime.now(), LocalDateTime.now());
        
        List<OrderItem> orderItems = Arrays.asList(
            new OrderItem(1L, "Test Product", 2, BigDecimal.valueOf(19.99))
        );
        testOrder = new Order(1L, 1L, "Delivery Address", orderItems, 
                             BigDecimal.valueOf(39.98), Order.Status.CREATED, 
                             LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create order from cart successfully")
    void shouldCreateOrderFromCartSuccessfully() {
        // Given
        when(cartRepositoryPort.findById(1L)).thenReturn(Optional.of(testCart));
        when(orderRepositoryPort.save(any(Order.class))).thenReturn(testOrder);
        when(cartRepositoryPort.save(any(Cart.class))).thenReturn(testCart);

        // When
        Order result = orderService.createFromCart(1L, "Delivery Address");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDeliveryAddress()).isEqualTo("Delivery Address");
        verify(cartRepositoryPort, times(1)).findById(1L);
        verify(orderRepositoryPort, times(1)).save(any(Order.class));
        verify(cartRepositoryPort, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when delivery address is null")
    void shouldThrowExceptionWhenDeliveryAddressIsNull() {
        // When/Then
        assertThatThrownBy(() -> orderService.createFromCart(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La dirección de entrega es obligatoria");
        verify(cartRepositoryPort, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when delivery address is blank")
    void shouldThrowExceptionWhenDeliveryAddressIsBlank() {
        // When/Then
        assertThatThrownBy(() -> orderService.createFromCart(1L, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La dirección de entrega es obligatoria");
        verify(cartRepositoryPort, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when cart not found")
    void shouldThrowExceptionWhenCartNotFound() {
        // Given
        when(cartRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> orderService.createFromCart(999L, "Address"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Carrito no encontrado");
        verify(orderRepositoryPort, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw InvalidOrderStateException when cart is empty")
    void shouldThrowExceptionWhenCartIsEmpty() {
        // Given
        Cart emptyCart = new Cart(1L, 1L, "user@example.com", "Test User", "+1234567890", 
                                 "Address", new ArrayList<>(), BigDecimal.ZERO, 
                                 LocalDateTime.now(), LocalDateTime.now());
        when(cartRepositoryPort.findById(1L)).thenReturn(Optional.of(emptyCart));

        // When/Then
        assertThatThrownBy(() -> orderService.createFromCart(1L, "Address"))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("carrito vacío");
        verify(orderRepositoryPort, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should return order when findById is called with valid id")
    void shouldReturnOrderById() {
        // Given
        when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(testOrder));

        // When
        Order result = orderService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(orderRepositoryPort, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when order not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        // Given
        when(orderRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> orderService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Orden no encontrada");
        verify(orderRepositoryPort, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should mark order as paid successfully")
    void shouldMarkOrderAsPaidSuccessfully() {
        // Given
        when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepositoryPort.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.markAsPaid(1L);

        // Then
        assertThat(result).isNotNull();
        verify(orderRepositoryPort, times(1)).findById(1L);
        verify(orderRepositoryPort, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw InvalidOrderStateException when marking cancelled order as paid")
    void shouldThrowExceptionWhenMarkingCancelledOrderAsPaid() {
        // Given
        testOrder.cancel();
        when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(testOrder));

        // When/Then
        assertThatThrownBy(() -> orderService.markAsPaid(1L))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("orden cancelada");
        verify(orderRepositoryPort, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should cancel order successfully")
    void shouldCancelOrderSuccessfully() {
        // Given
        when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepositoryPort.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.cancel(1L);

        // Then
        assertThat(result).isNotNull();
        verify(orderRepositoryPort, times(1)).findById(1L);
        verify(orderRepositoryPort, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw InvalidOrderStateException when cancelling paid order")
    void shouldThrowExceptionWhenCancellingPaidOrder() {
        // Given
        testOrder.markAsPaid();
        when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(testOrder));

        // When/Then
        assertThatThrownBy(() -> orderService.cancel(1L))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("orden pagada o completada");
        verify(orderRepositoryPort, never()).save(any(Order.class));
    }
}

