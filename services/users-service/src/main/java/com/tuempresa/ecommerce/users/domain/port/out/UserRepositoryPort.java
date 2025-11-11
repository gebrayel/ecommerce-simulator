package com.tuempresa.ecommerce.users.domain.port.out;

import com.tuempresa.ecommerce.users.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    List<User> findAll();
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    User save(User user);
    void deleteById(Long id);
    boolean existsByEmail(String email);
    boolean existsByTelefono(String telefono);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByTelefonoAndIdNot(String telefono, Long id);
}

