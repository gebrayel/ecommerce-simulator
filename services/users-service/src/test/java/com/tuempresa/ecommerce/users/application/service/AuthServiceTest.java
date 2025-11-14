package com.tuempresa.ecommerce.users.application.service;

import com.tuempresa.ecommerce.users.application.service.security.JwtTokenService;
import com.tuempresa.ecommerce.users.application.service.security.PasswordHashService;
import com.tuempresa.ecommerce.users.domain.exception.InvalidCredentialsException;
import com.tuempresa.ecommerce.users.domain.model.User;
import com.tuempresa.ecommerce.users.domain.port.in.dto.LoginCommand;
import com.tuempresa.ecommerce.users.domain.port.in.dto.LoginResult;
import com.tuempresa.ecommerce.users.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordHashService passwordHashService;

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginCommand loginCommand;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "test@example.com", "Test User", "+1234567890", 
                           "Test Address", "hashedPassword", LocalDateTime.now());
        loginCommand = new LoginCommand("test@example.com", "password123");
    }

    @Test
    @DisplayName("Should return LoginResult when credentials are valid")
    void shouldReturnLoginResultWhenCredentialsAreValid() {
        // Given
        String expectedToken = "jwt.token.here";
        when(userRepositoryPort.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordHashService.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtTokenService.generateToken(anyLong(), anyString(), any())).thenReturn(expectedToken);

        // When
        LoginResult result = authService.login(loginCommand);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getToken()).isEqualTo(expectedToken);
        verify(userRepositoryPort, times(1)).findByEmail("test@example.com");
        verify(passwordHashService, times(1)).matches("password123", "hashedPassword");
        verify(jwtTokenService, times(1)).generateToken(eq(1L), eq("test@example.com"), anyMap());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when user is not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepositoryPort.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> authService.login(loginCommand))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Credenciales inválidas");
        verify(userRepositoryPort, times(1)).findByEmail("test@example.com");
        verify(passwordHashService, never()).matches(anyString(), anyString());
        verify(jwtTokenService, never()).generateToken(anyLong(), anyString(), any());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when password is incorrect")
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        // Given
        when(userRepositoryPort.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordHashService.matches("password123", "hashedPassword")).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> authService.login(loginCommand))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Credenciales inválidas");
        verify(userRepositoryPort, times(1)).findByEmail("test@example.com");
        verify(passwordHashService, times(1)).matches("password123", "hashedPassword");
        verify(jwtTokenService, never()).generateToken(anyLong(), anyString(), any());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when passwordHash is null")
    void shouldThrowExceptionWhenPasswordHashIsNull() {
        // Given
        testUser.setPasswordHash(null);
        when(userRepositoryPort.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When/Then
        assertThatThrownBy(() -> authService.login(loginCommand))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Credenciales inválidas");
        verify(userRepositoryPort, times(1)).findByEmail("test@example.com");
        verify(passwordHashService, never()).matches(anyString(), anyString());
        verify(jwtTokenService, never()).generateToken(anyLong(), anyString(), any());
    }
}

