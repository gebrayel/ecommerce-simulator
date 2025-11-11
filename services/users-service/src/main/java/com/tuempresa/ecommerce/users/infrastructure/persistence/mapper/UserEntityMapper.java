package com.tuempresa.ecommerce.users.infrastructure.persistence.mapper;

import com.tuempresa.ecommerce.users.domain.model.User;
import com.tuempresa.ecommerce.users.infrastructure.persistence.entity.UserEntity;

public class UserEntityMapper {

    public static User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return new User(
            entity.getId(),
            entity.getEmail(),
            entity.getName(),
            entity.getTelefono(),
            entity.getDireccion(),
            entity.getCreatedAt()
        );
    }

    public static UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        UserEntity entity = new UserEntity(
            domain.getId(),
            domain.getEmail(),
            domain.getName(),
            domain.getTelefono(),
            domain.getDireccion(),
            domain.getCreatedAt()
        );
        return entity;
    }
}

