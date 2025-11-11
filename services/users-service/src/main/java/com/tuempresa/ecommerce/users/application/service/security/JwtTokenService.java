package com.tuempresa.ecommerce.users.application.service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenService {

    private final SecretKey secretKey;
    private final String issuer;
    private final long expirationMinutes;

    public JwtTokenService(@Value("${security.jwt.secret}") String secret,
                           @Value("${security.jwt.issuer:ecommerce-simulator}") String issuer,
                           @Value("${security.jwt.expiration-minutes:60}") long expirationMinutes) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(Long userId, String email, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        Instant expiration = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .addClaims(extraClaims)
                .claim("userId", userId)
                .claim("email", email)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}


