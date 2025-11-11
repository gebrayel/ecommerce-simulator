package com.tuempresa.ecommerce.orders.application.service.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class SensitiveDataHasher {

    private final MessageDigest messageDigest;

    public SensitiveDataHasher() {
        try {
            this.messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo inicializar el hash de datos sensibles", e);
        }
    }

    public synchronized String hash(String value) {
        messageDigest.reset();
        byte[] digest = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}


