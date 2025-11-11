package com.tuempresa.ecommerce.users.infrastructure.web.mapper;

import com.tuempresa.ecommerce.users.domain.model.User;
import com.tuempresa.ecommerce.users.infrastructure.web.dto.UserRequest;
import com.tuempresa.ecommerce.users.infrastructure.web.dto.UserResponse;

public class UserWebMapper {

    public static User toDomain(UserRequest request) {
        if (request == null) {
            return null;
        }
        User user = new User(
            request.getEmail(),
            request.getName(),
            request.getTelefono(),
            request.getDireccion()
        );
        user.setRawPassword(request.getPassword());
        return user;
    }

    public static UserResponse toResponse(User domain) {
        if (domain == null) {
            return null;
        }
        return new UserResponse(
            domain.getId(),
            domain.getName(),
            domain.getEmail(),
            domain.getTelefono(),
            domain.getDireccion(),
            domain.getCreatedAt()
        );
    }
}

