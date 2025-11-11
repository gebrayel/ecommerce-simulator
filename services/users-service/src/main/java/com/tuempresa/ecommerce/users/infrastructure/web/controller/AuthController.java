package com.tuempresa.ecommerce.users.infrastructure.web.controller;

import com.tuempresa.ecommerce.users.domain.port.in.AuthUseCase;
import com.tuempresa.ecommerce.users.domain.port.in.dto.LoginCommand;
import com.tuempresa.ecommerce.users.infrastructure.web.dto.LoginRequest;
import com.tuempresa.ecommerce.users.infrastructure.web.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = authUseCase.login(new LoginCommand(request.getEmail(), request.getPassword()));
        return ResponseEntity.ok(new LoginResponse(result.getUserId(), result.getEmail(), result.getToken()));
    }
}


