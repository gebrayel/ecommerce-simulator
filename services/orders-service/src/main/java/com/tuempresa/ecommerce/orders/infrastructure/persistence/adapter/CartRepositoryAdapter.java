package com.tuempresa.ecommerce.orders.infrastructure.persistence.adapter;

import com.tuempresa.ecommerce.orders.domain.model.Cart;
import com.tuempresa.ecommerce.orders.domain.port.out.CartRepositoryPort;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.CartEntity;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper.CartMapper;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.repository.CartJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CartRepositoryAdapter implements CartRepositoryPort {

    private final CartJpaRepository cartJpaRepository;

    public CartRepositoryAdapter(CartJpaRepository cartJpaRepository) {
        this.cartJpaRepository = cartJpaRepository;
    }

    @Override
    public Cart save(Cart cart) {
        CartEntity entity = CartMapper.toEntity(cart);
        CartEntity saved = cartJpaRepository.save(entity);
        return CartMapper.toDomain(saved);
    }

    @Override
    public Optional<Cart> findById(Long id) {
        return cartJpaRepository.findById(id)
                .map(CartMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        cartJpaRepository.deleteById(id);
    }
}


