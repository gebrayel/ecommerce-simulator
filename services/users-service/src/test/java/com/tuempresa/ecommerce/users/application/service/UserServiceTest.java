package com.tuempresa.ecommerce.users.application.service;

import com.tuempresa.ecommerce.users.application.service.security.PasswordHashService;
import com.tuempresa.ecommerce.users.domain.exception.DuplicateResourceException;
import com.tuempresa.ecommerce.users.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.users.domain.model.User;
import com.tuempresa.ecommerce.users.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private PasswordHashService passwordHashService;
    @Captor
    private ArgumentCaptor<User> userCaptor;

    @InjectMocks
    private UserService userService;

    @Test
    void findAll_returnsUsersFromRepository() {
        when(userRepositoryPort.findAll()).thenReturn(List.of(new User()));

        List<User> result = userService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void findById_returnsOptionalFromRepository() {
        User user = new User();
        when(userRepositoryPort.findById(5L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findById(5L);

        assertThat(result).contains(user);
    }

    @Test
    void create_hashesPasswordAndSaves_whenDataValid() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setTelefono("123456789");
        user.setRawPassword("secret");

        when(userRepositoryPort.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepositoryPort.existsByTelefono("123456789")).thenReturn(false);
        when(passwordHashService.hash("secret")).thenReturn("hashed");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.create(user);

        verify(passwordHashService).hash("secret");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.getRawPassword()).isNull();
    }

    @Test
    void create_throws_whenEmailExists() {
        User user = new User();
        user.setEmail("existing@example.com");
        user.setTelefono("123");
        user.setRawPassword("secret");

        when(userRepositoryPort.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(user))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void create_throws_whenPhoneExists() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setTelefono("123");
        user.setRawPassword("secret");

        when(userRepositoryPort.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepositoryPort.existsByTelefono("123")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(user))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void create_throws_whenPasswordMissing() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setTelefono("123");
        user.setRawPassword(" ");

        when(userRepositoryPort.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepositoryPort.existsByTelefono("123")).thenReturn(false);

        assertThatThrownBy(() -> userService.create(user))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_updatesFields_whenValid() {
        User existing = new User();
        existing.setId(1L);
        existing.setEmail("old@example.com");
        existing.setTelefono("987");

        User incoming = new User();
        incoming.setName("New Name");
        incoming.setEmail("new@example.com");
        incoming.setTelefono("555");
        incoming.setDireccion("Street");
        incoming.setRawPassword("newpass");

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepositoryPort.existsByEmailAndIdNot("new@example.com", 1L)).thenReturn(false);
        when(userRepositoryPort.existsByTelefonoAndIdNot("555", 1L)).thenReturn(false);
        when(passwordHashService.hash("newpass")).thenReturn("hashed");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.update(1L, incoming);

        verify(passwordHashService).hash("newpass");
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getDireccion()).isEqualTo("Street");
        assertThat(updated.getPasswordHash()).isEqualTo("hashed");
    }

    @Test
    void update_skipsPasswordHash_whenPasswordBlank() {
        User existing = new User();
        existing.setId(1L);
        existing.setEmail("old@example.com");
        existing.setTelefono("987");
        existing.setPasswordHash("oldHash");

        User incoming = new User();
        incoming.setEmail("old@example.com");
        incoming.setTelefono("987");
        incoming.setRawPassword("  ");

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepositoryPort.existsByEmailAndIdNot("old@example.com", 1L)).thenReturn(false);
        when(userRepositoryPort.existsByTelefonoAndIdNot("987", 1L)).thenReturn(false);
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.update(1L, incoming);

        verify(passwordHashService, never()).hash(any());
        assertThat(updated.getPasswordHash()).isEqualTo("oldHash");
    }

    @Test
    void update_throws_whenUserNotFound() {
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(1L, new User()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_throws_whenEmailDuplicate() {
        User existing = new User();
        existing.setId(1L);
        existing.setEmail("old@example.com");
        existing.setTelefono("987");

        User incoming = new User();
        incoming.setEmail("new@example.com");
        incoming.setTelefono("987");

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepositoryPort.existsByEmailAndIdNot("new@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.update(1L, incoming))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void update_throws_whenPhoneDuplicate() {
        User existing = new User();
        existing.setId(1L);
        existing.setEmail("old@example.com");
        existing.setTelefono("987");

        User incoming = new User();
        incoming.setEmail("old@example.com");
        incoming.setTelefono("123");

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepositoryPort.existsByEmailAndIdNot("old@example.com", 1L)).thenReturn(false);
        when(userRepositoryPort.existsByTelefonoAndIdNot("123", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.update(1L, incoming))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void delete_removesUser_whenExists() {
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(new User()));

        userService.delete(1L);

        verify(userRepositoryPort).deleteById(1L);
    }

    @Test
    void delete_throws_whenUserMissing() {
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(userRepositoryPort, never()).deleteById(any());
    }
}

