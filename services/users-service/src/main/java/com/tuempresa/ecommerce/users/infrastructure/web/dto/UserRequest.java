package com.tuempresa.ecommerce.users.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserRequest {
    
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "El teléfono debe contener entre 7 y 15 dígitos y puede iniciar con +")
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    public UserRequest() {
    }

    public UserRequest(String name, String email, String telefono, String direccion, String password) {
        this.name = name;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.password = password;
    }

    // Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

