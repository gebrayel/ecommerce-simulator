package com.tuempresa.ecommerce.users.infrastructure.web.dto;

import java.time.LocalDateTime;

public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String telefono;
    private String direccion;
    private LocalDateTime createdAt;

    public UserResponse() {
    }

    public UserResponse(Long id, String name, String email, String telefono, String direccion, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

