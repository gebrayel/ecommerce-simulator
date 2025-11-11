package com.tuempresa.ecommerce.orders.application.service.security;

import com.tuempresa.ecommerce.orders.domain.exception.UnauthorizedAccessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtService {

    private final SecretKey secretKey;
    private final String expectedIssuer;

    public JwtService(@Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.issuer:}") String expectedIssuer) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expectedIssuer = expectedIssuer;
    }

    public Long extractUserId(String authorizationHeader) {
        String jwt = extractBearerToken(authorizationHeader);
        Claims claims = parse(jwt);

        Object userIdClaim = claims.get("userId");
        if (userIdClaim instanceof Number number) {
            return number.longValue();
        }
        if (userIdClaim instanceof String s && !s.isBlank()) {
            return Long.parseLong(s);
        }

        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new UnauthorizedAccessException("El token JWT no contiene información de usuario");
        }
        return Long.parseLong(subject);
    }

    private Claims parse(String jwt) {
        try {
            var builder = Jwts.parserBuilder()
                    .setSigningKey(secretKey);
            if (hasIssuer()) {
                builder.requireIssuer(expectedIssuer);
            }
            Jws<Claims> parsed = builder.build().parseClaimsJws(jwt);
            return parsed.getBody();
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedAccessException("Token JWT inválido");
        }
    }

    private boolean hasIssuer() {
        return expectedIssuer != null && !expectedIssuer.isBlank();
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new UnauthorizedAccessException("Token JWT ausente");
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedAccessException("Formato de token JWT inválido");
        }
        return authorizationHeader.substring("Bearer ".length());
    }
}


