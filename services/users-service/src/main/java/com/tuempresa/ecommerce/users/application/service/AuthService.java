package com.tuempresa.ecommerce.users.application.service;

import com.tuempresa.ecommerce.users.application.service.security.JwtTokenService;
import com.tuempresa.ecommerce.users.application.service.security.PasswordHashService;
import com.tuempresa.ecommerce.users.domain.exception.InvalidCredentialsException;
import com.tuempresa.ecommerce.users.domain.model.User;
import com.tuempresa.ecommerce.users.domain.port.in.AuthUseCase;
import com.tuempresa.ecommerce.users.domain.port.in.dto.LoginCommand;
import com.tuempresa.ecommerce.users.domain.port.in.dto.LoginResult;
import com.tuempresa.ecommerce.users.domain.port.out.UserRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@Transactional(readOnly = true)
public class AuthService implements AuthUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHashService passwordHashService;
    private final JwtTokenService jwtTokenService;

    public AuthService(UserRepositoryPort userRepositoryPort,
                       PasswordHashService passwordHashService,
                       JwtTokenService jwtTokenService) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordHashService = passwordHashService;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public LoginResult login(LoginCommand command) {
        User user = userRepositoryPort.findByEmail(command.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        if (user.getPasswordHash() == null ||
                !passwordHashService.matches(command.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        String token = jwtTokenService.generateToken(
                user.getId(),
                user.getEmail(),
                Collections.emptyMap()
        );

        return new LoginResult(user.getId(), user.getEmail(), token);
    }
}


