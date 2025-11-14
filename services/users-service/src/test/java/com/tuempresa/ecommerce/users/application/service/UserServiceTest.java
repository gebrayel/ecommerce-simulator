package com.tuempresa.ecommerce.users.application.service;

import com.tuempresa.ecommerce.users.application.service.security.PasswordHashService;
import com.tuempresa.ecommerce.users.domain.exception.DuplicateResourceException;
import com.tuempresa.ecommerce.users.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.users.domain.model.User;
import com.tuempresa.ecommerce.users.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordHashService passwordHashService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User savedUser;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "Test User", "+1234567890", "Test Address");
        testUser.setRawPassword("password123");
        
        savedUser = new User(1L, "test@example.com", "Test User", "+1234567890", 
                            "Test Address", "hashedPassword", LocalDateTime.now());
    }

    @Test
    @DisplayName("Should return all users when findAll is called")
    void shouldReturnAllUsers() {
        // Given
        List<User> users = Arrays.asList(savedUser);
        when(userRepositoryPort.findAll()).thenReturn(users);

        // When
        List<User> result = userService.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("test@example.com");
        verify(userRepositoryPort, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return user when findById is called with valid id")
    void shouldReturnUserById() {
        // Given
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(savedUser));

        // When
        Optional<User> result = userService.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        verify(userRepositoryPort, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty when findById is called with non-existent id")
    void shouldReturnEmptyWhenUserNotFound() {
        // Given
        when(userRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(userRepositoryPort, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should create user successfully when email and telefono are unique")
    void shouldCreateUserSuccessfully() {
        // Given
        when(userRepositoryPort.existsByEmail(anyString())).thenReturn(false);
        when(userRepositoryPort.existsByTelefono(anyString())).thenReturn(false);
        when(passwordHashService.hash(anyString())).thenReturn("hashedPassword");
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userService.create(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPasswordHash()).isEqualTo("hashedPassword");
        verify(userRepositoryPort, times(1)).existsByEmail("test@example.com");
        verify(userRepositoryPort, times(1)).existsByTelefono("+1234567890");
        verify(passwordHashService, times(1)).hash("password123");
        verify(userRepositoryPort, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        when(userRepositoryPort.existsByEmail(anyString())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> userService.create(testUser))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("El email ya está registrado");
        verify(userRepositoryPort, times(1)).existsByEmail("test@example.com");
        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when telefono already exists")
    void shouldThrowExceptionWhenTelefonoExists() {
        // Given
        when(userRepositoryPort.existsByEmail(anyString())).thenReturn(false);
        when(userRepositoryPort.existsByTelefono(anyString())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> userService.create(testUser))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("El teléfono ya está registrado");
        verify(userRepositoryPort, times(1)).existsByEmail("test@example.com");
        verify(userRepositoryPort, times(1)).existsByTelefono("+1234567890");
        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when password is null")
    void shouldThrowExceptionWhenPasswordIsNull() {
        // Given
        testUser.setRawPassword(null);
        when(userRepositoryPort.existsByEmail(anyString())).thenReturn(false);
        when(userRepositoryPort.existsByTelefono(anyString())).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> userService.create(testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La contraseña es obligatoria");
        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when password is blank")
    void shouldThrowExceptionWhenPasswordIsBlank() {
        // Given
        testUser.setRawPassword("   ");
        when(userRepositoryPort.existsByEmail(anyString())).thenReturn(false);
        when(userRepositoryPort.existsByTelefono(anyString())).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> userService.create(testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La contraseña es obligatoria");
        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should update user successfully when valid data is provided")
    void shouldUpdateUserSuccessfully() {
        // Given
        User updatedUser = new User("updated@example.com", "Updated User", "+9876543210", "Updated Address");
        updatedUser.setRawPassword("newPassword123");
        
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(savedUser));
        when(userRepositoryPort.existsByEmailAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(userRepositoryPort.existsByTelefonoAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(passwordHashService.hash(anyString())).thenReturn("newHashedPassword");
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userService.update(1L, updatedUser);

        // Then
        assertThat(result).isNotNull();
        verify(userRepositoryPort, times(1)).findById(1L);
        verify(userRepositoryPort, times(1)).existsByEmailAndIdNot("updated@example.com", 1L);
        verify(userRepositoryPort, times(1)).existsByTelefonoAndIdNot("+9876543210", 1L);
        verify(passwordHashService, times(1)).hash("newPassword123");
        verify(userRepositoryPort, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent user")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        // Given
        when(userRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.update(999L, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
        verify(userRepositoryPort, times(1)).findById(999L);
        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when updating with duplicate email")
    void shouldThrowExceptionWhenUpdatingWithDuplicateEmail() {
        // Given
        User updatedUser = new User("duplicate@example.com", "Updated User", "+1234567890", "Updated Address");
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(savedUser));
        when(userRepositoryPort.existsByEmailAndIdNot(anyString(), anyLong())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> userService.update(1L, updatedUser))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("El email ya está registrado");
        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should delete user successfully when user exists")
    void shouldDeleteUserSuccessfully() {
        // Given
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(savedUser));
        doNothing().when(userRepositoryPort).deleteById(1L);

        // When
        userService.delete(1L);

        // Then
        verify(userRepositoryPort, times(1)).findById(1L);
        verify(userRepositoryPort, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent user")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // Given
        when(userRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
        verify(userRepositoryPort, times(1)).findById(999L);
        verify(userRepositoryPort, never()).deleteById(anyLong());
    }
}

