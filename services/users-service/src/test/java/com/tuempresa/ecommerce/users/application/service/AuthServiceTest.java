package com.tuempresa.ecommerce.users.application.service;

import com.tuempresa.ecommerce.users.application.service.security.JwtTokenService;
import com.tuempresa.ecommerce.users.application.service.security.PasswordHashService;
import com.tuempresa.ecommerce.users.domain.exception.InvalidCredentialsException;
import com.tuempresa.ecommerce.users.domain.model.User;
import com.tuempresa.ecommerce.users.domain.port.in.dto.LoginCommand;
import com.tuempresa.ecommerce.users.domain.port.in.dto.LoginResult;
import com.tuempresa.ecommerce.users.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private PasswordHashService passwordHashService;
    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_returnsToken_whenCredentialsValid() {
        User user = new User();
        user.setId(10L);
        user.setEmail("user@example.com");
        user.setPasswordHash("hashed");

        when(userRepositoryPort.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordHashService.matches("password", "hashed")).thenReturn(true);
        when(jwtTokenService.generateToken(10L, "user@example.com", java.util.Collections.emptyMap()))
                .thenReturn("jwt-token");

        LoginResult result = authService.login(new LoginCommand("user@example.com", "password"));

        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getEmail()).isEqualTo("user@example.com");
        assertThat(result.getToken()).isEqualTo("jwt-token");
        verify(jwtTokenService).generateToken(10L, "user@example.com", java.util.Collections.emptyMap());
    }

    @Test
    void login_throws_whenUserNotFound() {
        when(userRepositoryPort.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginCommand("missing@example.com", "password")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_throws_whenPasswordDoesNotMatch() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPasswordHash("hashed");
        when(userRepositoryPort.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordHashService.matches("password", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginCommand("user@example.com", "password")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}

