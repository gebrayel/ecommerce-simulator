package com.tuempresa.ecommerce.orders.application.service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.UUID;

@Component
public class CardTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final byte[] secret;

    public CardTokenService(@Value("${security.card-token.secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public GeneratedToken generate(Long cardId, Long userId) {
        String tokenId = UUID.randomUUID().toString();
        String payload = payload(cardId, userId, tokenId);
        String signature = sign(payload);
        String token = cardId + "." + tokenId + "." + signature;
        return new GeneratedToken(token, tokenId, signature);
    }

    public boolean isValid(Long cardId, Long userId, String tokenId, String providedSignature) {
        String payload = payload(cardId, userId, tokenId);
        String expectedSignature = sign(payload);
        return constantTimeEquals(expectedSignature, providedSignature);
    }

    public TokenComponents parse(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Formato de token de tarjeta inválido");
        }
        Long cardId = Long.parseLong(parts[0]);
        String tokenId = parts[1];
        String signature = parts[2];
        return new TokenComponents(cardId, tokenId, signature);
    }

    private String payload(Long cardId, Long userId, String tokenId) {
        return cardId + ":" + userId + ":" + tokenId;
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No fue posible firmar el token de tarjeta", e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    public record GeneratedToken(String token, String tokenId, String signature) {
    }

    public record TokenComponents(Long cardId, String tokenId, String signature) {
    }
}


