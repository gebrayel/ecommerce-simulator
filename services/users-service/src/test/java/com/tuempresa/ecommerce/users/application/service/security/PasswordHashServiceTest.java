package com.tuempresa.ecommerce.users.application.service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PasswordHashService Tests")
class PasswordHashServiceTest {

    private PasswordHashService passwordHashService;

    @BeforeEach
    void setUp() {
        passwordHashService = new PasswordHashService();
    }

    @Test
    @DisplayName("Should hash password successfully")
    void shouldHashPasswordSuccessfully() {
        // Given
        String rawPassword = "password123";

        // When
        String hashedPassword = passwordHashService.hash(rawPassword);

        // Then
        assertThat(hashedPassword).isNotNull();
        assertThat(hashedPassword).isNotEqualTo(rawPassword);
        assertThat(hashedPassword).hasSizeGreaterThan(50); // BCrypt hash length
    }

    @Test
    @DisplayName("Should produce different hashes for same password")
    void shouldProduceDifferentHashesForSamePassword() {
        // Given
        String rawPassword = "password123";

        // When
        String hash1 = passwordHashService.hash(rawPassword);
        String hash2 = passwordHashService.hash(rawPassword);

        // Then
        assertThat(hash1).isNotEqualTo(hash2); // BCrypt uses salt, so hashes differ
    }

    @Test
    @DisplayName("Should match password correctly when password is correct")
    void shouldMatchPasswordWhenCorrect() {
        // Given
        String rawPassword = "password123";
        String hashedPassword = passwordHashService.hash(rawPassword);

        // When
        boolean matches = passwordHashService.matches(rawPassword, hashedPassword);

        // Then
        assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("Should not match password when password is incorrect")
    void shouldNotMatchPasswordWhenIncorrect() {
        // Given
        String correctPassword = "password123";
        String incorrectPassword = "wrongpassword";
        String hashedPassword = passwordHashService.hash(correctPassword);

        // When
        boolean matches = passwordHashService.matches(incorrectPassword, hashedPassword);

        // Then
        assertThat(matches).isFalse();
    }

    @Test
    @DisplayName("Should handle empty password")
    void shouldHandleEmptyPassword() {
        // Given
        String emptyPassword = "";

        // When
        String hashedPassword = passwordHashService.hash(emptyPassword);
        boolean matches = passwordHashService.matches(emptyPassword, hashedPassword);

        // Then
        assertThat(hashedPassword).isNotNull();
        assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("Should handle special characters in password")
    void shouldHandleSpecialCharacters() {
        // Given
        String passwordWithSpecialChars = "p@ssw0rd!#$%";

        // When
        String hashedPassword = passwordHashService.hash(passwordWithSpecialChars);
        boolean matches = passwordHashService.matches(passwordWithSpecialChars, hashedPassword);

        // Then
        assertThat(hashedPassword).isNotNull();
        assertThat(matches).isTrue();
    }
}

