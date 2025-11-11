package com.tuempresa.ecommerce.users.application.service;

import com.tuempresa.ecommerce.users.domain.exception.DuplicateResourceException;
import com.tuempresa.ecommerce.users.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.users.domain.model.User;
import com.tuempresa.ecommerce.users.domain.port.in.UserUseCase;
import com.tuempresa.ecommerce.users.domain.port.out.UserRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public UserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepositoryPort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepositoryPort.findById(id);
    }

    @Override
    public User create(User user) {
        // Validar que email y telefono sean únicos
        if (userRepositoryPort.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("El email ya está registrado: " + user.getEmail());
        }
        if (userRepositoryPort.existsByTelefono(user.getTelefono())) {
            throw new DuplicateResourceException("El teléfono ya está registrado: " + user.getTelefono());
        }
        return userRepositoryPort.save(user);
    }

    @Override
    public User update(Long id, User user) {
        Optional<User> existingUserOpt = userRepositoryPort.findById(id);
        if (existingUserOpt.isEmpty()) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }

        User existingUser = existingUserOpt.get();

        // Validar que email sea único (excepto el usuario actual)
        if (!existingUser.getEmail().equals(user.getEmail()) && 
            userRepositoryPort.existsByEmailAndIdNot(user.getEmail(), id)) {
            throw new DuplicateResourceException("El email ya está registrado: " + user.getEmail());
        }

        // Validar que telefono sea único (excepto el usuario actual)
        if (!existingUser.getTelefono().equals(user.getTelefono()) && 
            userRepositoryPort.existsByTelefonoAndIdNot(user.getTelefono(), id)) {
            throw new DuplicateResourceException("El teléfono ya está registrado: " + user.getTelefono());
        }

        // Actualizar campos
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setTelefono(user.getTelefono());
        existingUser.setDireccion(user.getDireccion());

        return userRepositoryPort.save(existingUser);
    }

    @Override
    public void delete(Long id) {
        if (userRepositoryPort.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }
        userRepositoryPort.deleteById(id);
    }
}

