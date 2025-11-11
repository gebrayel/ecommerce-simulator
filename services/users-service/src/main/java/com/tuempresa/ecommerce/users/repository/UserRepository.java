package com.tuempresa.ecommerce.users.repository;

import com.tuempresa.ecommerce.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}

