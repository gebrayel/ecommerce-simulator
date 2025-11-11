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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
class CartServiceTest {

    @Mock
    private CartRepositoryPort cartRepositoryPort;
    @Mock
    private UserClientPort userClientPort;
    @Mock
    private ProductCatalogClientPort productCatalogClientPort;
    @Captor
    private ArgumentCaptor<Cart> cartCaptor;

    @InjectMocks
    private CartService cartService;

    private UserSnapshot userSnapshot;

    @BeforeEach
    void setUp() {
        userSnapshot = new UserSnapshot(1L, "user@example.com", "User", "123", "Street 1");
    }

    @Test
    void createCart_buildsCartFromUserSnapshot() {
        when(userClientPort.findById(1L)).thenReturn(Optional.of(userSnapshot));
        when(cartRepositoryPort.save(any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, Cart.class));

        Cart result = cartService.createCart(1L);

        verify(cartRepositoryPort).save(cartCaptor.capture());
        Cart captured = cartCaptor.getValue();
        assertThat(captured.getUserId()).isEqualTo(userSnapshot.getId());
        assertThat(captured.getUserEmail()).isEqualTo(userSnapshot.getEmail());
        assertThat(captured.getUserName()).isEqualTo(userSnapshot.getName());
        assertThat(captured.getUserAddress()).isEqualTo(userSnapshot.getAddress());
        assertThat(captured.getCreatedAt()).isNotNull();
        assertThat(captured.getUpdatedAt()).isNotNull();
        assertThat(captured.getItems()).isEmpty();
        assertThat(result).isSameAs(captured);
    }

    @Test
    void createCart_throwsException_whenUserMissing() {
        when(userClientPort.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.createCart(2L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(cartRepositoryPort, never()).save(any());
    }

    @Test
    void addItem_addsNewItem_whenNotPresent() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(1L);

        ProductSnapshot productSnapshot = new ProductSnapshot(3L, "Laptop", "Gaming", BigDecimal.TEN);

        when(cartRepositoryPort.findById(1L)).thenReturn(Optional.of(cart));
        when(productCatalogClientPort.findById(3L)).thenReturn(Optional.of(productSnapshot));
        when(cartRepositoryPort.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart updated = cartService.addItem(1L, 3L, 2);

        assertThat(updated.getItems()).hasSize(1);
        verify(cartRepositoryPort).save(cartCaptor.capture());
        Cart persisted = cartCaptor.getValue();
        assertThat(persisted.getItems()).hasSize(1);
        CartItem item = persisted.getItems().get(0);
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getProductName()).isEqualTo("Laptop");
        assertThat(persisted.getUpdatedAt()).isNotNull();
        assertThat(updated).isSameAs(persisted);
    }

    @Test
    void addItem_updatesQuantity_whenItemAlreadyPresent() {
        CartItem existing = new CartItem(3L, "Laptop", "Gaming", 1, BigDecimal.TEN);
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setItems(List.of(existing));

        ProductSnapshot snapshot = new ProductSnapshot(3L, "Laptop Updated", "Gaming", BigDecimal.TEN);
        when(cartRepositoryPort.findById(1L)).thenReturn(Optional.of(cart));
        when(productCatalogClientPort.findById(3L)).thenReturn(Optional.of(snapshot));
        when(cartRepositoryPort.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.addItem(1L, 3L, 4);

        verify(cartRepositoryPort).save(cartCaptor.capture());
        Cart persisted = cartCaptor.getValue();
        assertThat(persisted.getItems()).hasSize(1);
        CartItem item = persisted.getItems().get(0);
        assertThat(item.getQuantity()).isEqualTo(5);
        assertThat(item.getProductName()).isEqualTo("Laptop Updated");
        assertThat(result).isSameAs(persisted);
    }

    @Test
    void removeItem_deletesItemAndPersists() {
        CartItem existing = new CartItem(3L, "Laptop", "Gaming", 1, BigDecimal.TEN);
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setItems(List.of(existing));

        when(cartRepositoryPort.findById(1L)).thenReturn(Optional.of(cart));
        when(cartRepositoryPort.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cartService.removeItem(1L, 3L);

        verify(cartRepositoryPort).save(cartCaptor.capture());
        assertThat(cartCaptor.getValue().getItems()).isEmpty();
    }

    @Test
    void clearCart_removesAllItems() {
        CartItem existing = new CartItem(3L, "Laptop", "Gaming", 1, BigDecimal.TEN);
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setItems(List.of(existing));

        when(cartRepositoryPort.findById(1L)).thenReturn(Optional.of(cart));
        when(cartRepositoryPort.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cartService.clearCart(1L);

        verify(cartRepositoryPort).save(cartCaptor.capture());
        Cart persisted = cartCaptor.getValue();
        assertThat(persisted.getItems()).isEmpty();
        assertThat(persisted.getUpdatedAt()).isNotNull();
    }

    @Test
    void getById_returnsCart_whenExists() {
        Cart cart = new Cart();
        cart.setId(7L);
        when(cartRepositoryPort.findById(7L)).thenReturn(Optional.of(cart));

        Cart result = cartService.getById(7L);

        assertThat(result).isEqualTo(cart);
    }
}

