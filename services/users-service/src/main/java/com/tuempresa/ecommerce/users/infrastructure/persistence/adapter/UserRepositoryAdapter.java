package com.tuempresa.ecommerce.users.infrastructure.persistence.adapter;

import com.tuempresa.ecommerce.users.domain.model.User;
import com.tuempresa.ecommerce.users.domain.port.out.UserRepositoryPort;
import com.tuempresa.ecommerce.users.infrastructure.persistence.entity.UserEntity;
import com.tuempresa.ecommerce.users.infrastructure.persistence.mapper.UserEntityMapper;
import com.tuempresa.ecommerce.users.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll().stream()
                .map(UserEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id)
                .map(UserEntityMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(UserEntityMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserEntityMapper.toEntity(user);
        UserEntity savedEntity = userJpaRepository.save(entity);
        return UserEntityMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(Long id) {
        userJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByTelefono(String telefono) {
        return userJpaRepository.existsByTelefono(telefono);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, Long id) {
        return userJpaRepository.existsByEmailAndIdNot(email, id);
    }

    @Override
    public boolean existsByTelefonoAndIdNot(String telefono, Long id) {
        return userJpaRepository.existsByTelefonoAndIdNot(telefono, id);
    }
}

