package com.tuempresa.ecommerce.users.infrastructure.web.filter;

import com.tuempresa.ecommerce.users.infrastructure.persistence.entity.RequestLogEntity;
import com.tuempresa.ecommerce.users.infrastructure.persistence.repository.RequestLogRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final RequestLogRepository requestLogRepository;
    private final String serviceName;
    private final SecretKey signingKey;

    public RequestLoggingFilter(RequestLogRepository requestLogRepository,
                                @Value("${spring.application.name:users-service}") String serviceName,
                                @Value("${security.jwt.secret:}") String jwtSecret) {
        this.requestLogRepository = requestLogRepository;
        this.serviceName = serviceName;
        if (jwtSecret != null && !jwtSecret.isBlank()) {
            this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        } else {
            this.signingKey = null;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            RequestLogEntity log = new RequestLogEntity();
            log.setId(UUID.randomUUID());
            log.setTimestamp(LocalDateTime.now());
            log.setServiceName(serviceName);
            log.setEndpoint(request.getRequestURI());
            log.setHttpMethod(request.getMethod());
            log.setStatusCode(response.getStatus());
            log.setUserIdentifier(resolveUserIdentifier(request));
            log.setMessage(buildMessage(request));
            requestLogRepository.save(log);
        }
    }

    private String buildMessage(HttpServletRequest request) {
        String query = request.getQueryString();
        return query != null ? "query=" + query : "N/A";
    }

    private String resolveUserIdentifier(HttpServletRequest request) {
        String headerUser = request.getHeader("X-User-Id");
        if (headerUser != null && !headerUser.isBlank()) {
            return headerUser;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank() || !authorization.startsWith("Bearer ")) {
            return null;
        }

        if (signingKey == null) {
            return null;
        }

        try {
            String token = authorization.substring("Bearer ".length());
            Jws<Claims> parsed = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);

            Claims claims = parsed.getBody();
            return Optional.ofNullable(claims.get("userId"))
                    .map(Object::toString)
                    .orElseGet(claims::getSubject);
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }
}


