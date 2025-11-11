package com.tuempresa.ecommerce.users.infrastructure.persistence.repository;

import com.tuempresa.ecommerce.users.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmail(String email);
    boolean existsByTelefono(String telefono);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByTelefonoAndIdNot(String telefono, Long id);
}

