package com.tuempresa.ecommerce.users.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class UsersController {

    @GetMapping("/users")
    public ResponseEntity<String> getUsers() {
        return ResponseEntity.status(HttpStatus.OK)
                .body("lista completa de usuarios");
    }
}

