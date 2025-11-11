package com.tuempresa.ecommerce.users.domain.model;

import java.time.LocalDateTime;

public class User {
    private Long id;
    private String email;
    private String name;
    private String telefono;
    private String direccion;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(Long id, String email, String name, String telefono, String direccion, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.telefono = telefono;
        this.direccion = direccion;
        this.createdAt = createdAt;
    }

    public User(String email, String name, String telefono, String direccion) {
        this.email = email;
        this.name = name;
        this.telefono = telefono;
        this.direccion = direccion;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

