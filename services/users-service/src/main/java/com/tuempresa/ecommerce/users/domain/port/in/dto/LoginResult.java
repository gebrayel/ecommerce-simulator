package com.tuempresa.ecommerce.users.domain.port.in.dto;

public class LoginResult {

    private final Long userId;
    private final String email;
    private final String token;

    public LoginResult(Long userId, String email, String token) {
        this.userId = userId;
        this.email = email;
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }
}


