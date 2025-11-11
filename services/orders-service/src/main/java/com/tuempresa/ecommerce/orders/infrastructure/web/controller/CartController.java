package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import com.tuempresa.ecommerce.orders.domain.port.in.CartUseCase;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.AddCartItemRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CartResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreateCartRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.mapper.CartWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/carts")
public class CartController {

    private final CartUseCase cartService;

    public CartController(CartUseCase cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<CartResponse> createCart(@Valid @RequestBody CreateCartRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CartWebMapper.toResponse(cartService.createCart(request.getUserId())));
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartResponse> addItem(@PathVariable Long cartId,
                                                @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(
                CartWebMapper.toResponse(
                        cartService.addItem(cartId, request.getProductId(), request.getQuantity())
                )
        );
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long cartId,
                                                   @PathVariable Long productId) {
        return ResponseEntity.ok(
                CartWebMapper.toResponse(cartService.removeItem(cartId, productId))
        );
    }

    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<CartResponse> clearCart(@PathVariable Long cartId) {
        return ResponseEntity.ok(
                CartWebMapper.toResponse(cartService.clearCart(cartId))
        );
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long cartId) {
        return ResponseEntity.ok(
                CartWebMapper.toResponse(cartService.getById(cartId))
        );
    }
}


