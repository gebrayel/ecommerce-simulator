package com.tuempresa.ecommerce.users.application.service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenService Tests")
class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;
    private String secretKey;

    @BeforeEach
    void setUp() {
        secretKey = "mySecretKeyThatIsAtLeast256BitsLongForHS256AlgorithmToWorkProperly";
        jwtTokenService = new JwtTokenService(secretKey, "ecommerce-simulator", 60);
    }

    @Test
    @DisplayName("Should generate token successfully")
    void shouldGenerateTokenSuccessfully() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        Map<String, Object> extraClaims = new HashMap<>();

        // When
        String token = jwtTokenService.generateToken(userId, email, extraClaims);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts (header.payload.signature)
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void shouldGenerateDifferentTokensForDifferentUsers() {
        // Given
        Long userId1 = 1L;
        Long userId2 = 2L;
        String email = "test@example.com";
        Map<String, Object> extraClaims = new HashMap<>();

        // When
        String token1 = jwtTokenService.generateToken(userId1, email, extraClaims);
        String token2 = jwtTokenService.generateToken(userId2, email, extraClaims);

        // Then
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("Should generate different tokens for same user at different times")
    void shouldGenerateDifferentTokensForSameUserAtDifferentTimes() throws InterruptedException {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        Map<String, Object> extraClaims = new HashMap<>();

        // When
        String token1 = jwtTokenService.generateToken(userId, email, extraClaims);
        Thread.sleep(1100); // Delay of more than 1 second to ensure different issuedAt time
        String token2 = jwtTokenService.generateToken(userId, email, extraClaims);

        // Then
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("Should generate token with extra claims")
    void shouldGenerateTokenWithExtraClaims() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        extraClaims.put("permissions", "read,write");

        // When
        String token = jwtTokenService.generateToken(userId, email, extraClaims);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Should generate token with empty extra claims")
    void shouldGenerateTokenWithEmptyExtraClaims() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        Map<String, Object> extraClaims = new HashMap<>();

        // When
        String token = jwtTokenService.generateToken(userId, email, extraClaims);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("Should generate valid JWT format")
    void shouldGenerateValidJWTFormat() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        Map<String, Object> extraClaims = new HashMap<>();

        // When
        String token = jwtTokenService.generateToken(userId, email, extraClaims);

        // Then
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
        // Each part should be base64url encoded (non-empty)
        assertThat(parts[0]).isNotEmpty(); // Header
        assertThat(parts[1]).isNotEmpty(); // Payload
        assertThat(parts[2]).isNotEmpty(); // Signature
    }
}

