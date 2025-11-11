package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.model.Cart;
import com.tuempresa.ecommerce.orders.domain.model.CartItem;
import com.tuempresa.ecommerce.orders.domain.model.ProductSnapshot;
import com.tuempresa.ecommerce.orders.domain.model.UserSnapshot;
import com.tuempresa.ecommerce.orders.domain.port.in.CartUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.CartRepositoryPort;
import com.tuempresa.ecommerce.orders.domain.port.out.ProductCatalogClientPort;
import com.tuempresa.ecommerce.orders.domain.port.out.UserClientPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartService implements CartUseCase {

    private final CartRepositoryPort cartRepositoryPort;
    private final UserClientPort userClientPort;
    private final ProductCatalogClientPort productCatalogClientPort;

    public CartService(CartRepositoryPort cartRepositoryPort,
                       UserClientPort userClientPort,
                       ProductCatalogClientPort productCatalogClientPort) {
        this.cartRepositoryPort = cartRepositoryPort;
        this.userClientPort = userClientPort;
        this.productCatalogClientPort = productCatalogClientPort;
    }

    @Override
    public Cart createCart(Long userId) {
        UserSnapshot user = userClientPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        Cart cart = new Cart(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getAddress()
        );
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepositoryPort.save(cart);
    }

    @Override
    public Cart addItem(Long cartId, Long productId, Integer quantity) {
        Cart cart = getOrThrow(cartId);
        List<CartItem> items = new ArrayList<>(cart.getItems());

        ProductSnapshot product = productCatalogClientPort.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productId));

        CartItem newItem = new CartItem(
                product.getId(),
                product.getName(),
                product.getDescription(),
                quantity,
                product.getPrice()
        );

        Optional<CartItem> existingItem = items.stream()
                .filter(cartItem -> cartItem.getProductId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setProductName(newItem.getProductName());
            cartItem.setProductDescription(newItem.getProductDescription());
            cartItem.setPrice(newItem.getPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            items.add(newItem);
        }

        cart.setItems(items);
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepositoryPort.save(cart);
    }

    @Override
    public Cart removeItem(Long cartId, Long productId) {
        Cart cart = getOrThrow(cartId);
        cart.removeItem(productId);
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepositoryPort.save(cart);
    }

    @Override
    public Cart clearCart(Long cartId) {
        Cart cart = getOrThrow(cartId);
        cart.clearItems();
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepositoryPort.save(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public Cart getById(Long cartId) {
        return getOrThrow(cartId);
    }

    private Cart getOrThrow(Long cartId) {
        return cartRepositoryPort.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado con ID: " + cartId));
    }
}


