package com.tuempresa.ecommerce.users.domain.port.in;

import com.tuempresa.ecommerce.users.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserUseCase {
    List<User> findAll();
    Optional<User> findById(Long id);
    User create(User user);
    User update(Long id, User user);
    void delete(Long id);
}

