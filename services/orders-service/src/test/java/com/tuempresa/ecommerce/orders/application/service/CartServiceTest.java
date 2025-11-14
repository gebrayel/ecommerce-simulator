package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.model.Cart;
import com.tuempresa.ecommerce.orders.domain.model.CartItem;
import com.tuempresa.ecommerce.orders.domain.model.ProductSnapshot;
import com.tuempresa.ecommerce.orders.domain.model.UserSnapshot;
import com.tuempresa.ecommerce.orders.domain.port.out.CartRepositoryPort;
import com.tuempresa.ecommerce.orders.domain.port.out.ProductCatalogClientPort;
import com.tuempresa.ecommerce.orders.domain.port.out.UserClientPort;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Tests")
class CartServiceTest {

    @Mock
    private CartRepositoryPort cartRepositoryPort;

    @Mock
    private UserClientPort userClientPort;

    @Mock
    private ProductCatalogClientPort productCatalogClientPort;

    @InjectMocks
    private CartService cartService;

    private Cart testCart;
    private UserSnapshot testUser;
    private ProductSnapshot testProduct;

    @BeforeEach
    void setUp() {
        testUser = new UserSnapshot(1L, "user@example.com", "Test User", "+1234567890", "Address");
        testProduct = new ProductSnapshot(1L, "Test Product", "Description", BigDecimal.valueOf(19.99));
        testCart = new Cart(1L, 1L, "user@example.com", "Test User", "+1234567890", 
                           "Address", new ArrayList<>(), BigDecimal.ZERO, 
                           LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create cart successfully")
    void shouldCreateCartSuccessfully() {
        // Given
        when(userClientPort.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepositoryPort.save(any(Cart.class))).thenReturn(testCart);

        // When
        Cart result = cartService.createCart(1L);

        // Then
        assertThat(result).isNotNull();
        verify(userClientPort, times(1)).findById(1L);
        verify(cartRepositoryPort, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userClientPort.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> cartService.createCart(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
        verify(cartRepositoryPort, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should add item to cart successfully")
    void shouldAddItemToCartSuccessfully() {
        // Given
        when(cartRepositoryPort.findById(1L)).thenReturn(Optional.of(testCart));
        when(productCatalogClientPort.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepositoryPort.save(any(Cart.class))).thenReturn(testCart);

        // When
        Cart result = cartService.addItem(1L, 1L, 2);

        // Then
        assertThat(result).isNotNull();
        verify(cartRepositoryPort, times(1)).findById(1L);
        verify(productCatalogClientPort, times(1)).findById(1L);
        verify(cartRepositoryPort, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when cart not found for addItem")
    void shouldThrowExceptionWhenCartNotFoundForAddItem() {
        // Given
        when(cartRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> cartService.addItem(999L, 1L, 2))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Carrito no encontrado");
        verify(productCatalogClientPort, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product not found")
    void shouldThrowExceptionWhenProductNotFound() {
        // Given
        when(cartRepositoryPort.findById(1L)).thenReturn(Optional.of(testCart));
        when(productCatalogClientPort.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> cartService.addItem(1L, 999L, 2))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Producto no encontrado");
        verify(cartRepositoryPort, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should remove item from cart successfully")
    void shouldRemoveItemFromCartSuccessfully() {
        // Given
        when(cartRepositoryPort.findById(1L)).thenReturn(Optional.of(testCart));
        when(cartRepositoryPort.save(any(Cart.class))).thenReturn(testCart);

        // When
        Cart result = cartService.removeItem(1L, 1L);

        // Then
        assertThat(result).isNotNull();
        verify(cartRepositoryPort, times(1)).findById(1L);
        verify(cartRepositoryPort, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should clear cart successfully")
    void shouldClearCartSuccessfully() {
        // Given
        when(cartRepositoryPort.findById(1L)).thenReturn(Optional.of(testCart));
        when(cartRepositoryPort.save(any(Cart.class))).thenReturn(testCart);

        // When
        Cart result = cartService.clearCart(1L);

        // Then
        assertThat(result).isNotNull();
        verify(cartRepositoryPort, times(1)).findById(1L);
        verify(cartRepositoryPort, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should return cart when getById is called with valid id")
    void shouldReturnCartById() {
        // Given
        when(cartRepositoryPort.findById(1L)).thenReturn(Optional.of(testCart));

        // When
        Cart result = cartService.getById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(cartRepositoryPort, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when cart not found for getById")
    void shouldThrowExceptionWhenCartNotFoundForGetById() {
        // Given
        when(cartRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> cartService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Carrito no encontrado");
    }
}

