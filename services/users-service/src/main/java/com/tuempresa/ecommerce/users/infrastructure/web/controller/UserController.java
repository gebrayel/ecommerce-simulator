package com.tuempresa.ecommerce.users.infrastructure.web.controller;

import com.tuempresa.ecommerce.users.domain.model.User;
import com.tuempresa.ecommerce.users.domain.port.in.UserUseCase;
import com.tuempresa.ecommerce.users.infrastructure.web.dto.UserRequest;
import com.tuempresa.ecommerce.users.infrastructure.web.dto.UserResponse;
import com.tuempresa.ecommerce.users.infrastructure.web.mapper.UserWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserUseCase userUseCase;

    public UserController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userUseCase.findAll().stream()
                .map(UserWebMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return userUseCase.findById(id)
                .map(user -> ResponseEntity.status(HttpStatus.OK).body(UserWebMapper.toResponse(user)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        User user = UserWebMapper.toDomain(request);
        User createdUser = userUseCase.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserWebMapper.toResponse(createdUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        User user = UserWebMapper.toDomain(request);
        User updatedUser = userUseCase.update(id, user);
        return ResponseEntity.status(HttpStatus.OK).body(UserWebMapper.toResponse(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userUseCase.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

