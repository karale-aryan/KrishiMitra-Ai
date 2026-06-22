package com.krishimitra.auth.internal;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * Utility component for generating and validating JWT tokens.
 * Uses the jjwt 0.12.x API with HMAC-SHA256 signing.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${krishimitra.jwt.secret}")
    private String jwtSecret;

    @Value("${krishimitra.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${krishimitra.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes;
        try {
            // Try to decode as Base64 first (standard usage)
            keyBytes = Base64.getDecoder().decode(jwtSecret.trim());
        } catch (IllegalArgumentException e) {
            // Fall back to raw UTF-8 bytes if the secret is plain-text
            log.warn("JWT secret is not valid Base64; using raw UTF-8 bytes.");
            keyBytes = jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }

        // Ensure keyBytes is at least 256 bits (32 bytes) for HMAC-SHA256.
        // If it is shorter, or to be absolutely secure against configuration variations,
        // we run it through SHA-256 to deterministically produce a strong 32-byte key.
        if (keyBytes.length < 32) {
            try {
                keyBytes = java.security.MessageDigest.getInstance("SHA-256").digest(keyBytes);
            } catch (java.security.NoSuchAlgorithmException e) {
                throw new IllegalStateException("SHA-256 algorithm not available", e);
            }
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate an access token for the given user.
     * Contains claims: role, phoneNumber.
     */
    public String generateAccessToken(UserEntity user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .claim("phoneNumber", user.getPhoneNumber())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Generate a refresh token for the given user with a longer expiry.
     */
    public String generateRefreshToken(UserEntity user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extract the user ID (UUID) from the token subject.
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return UUID.fromString(claims.getSubject());
    }

    /**
     * Validate a JWT token. Returns true if the token is valid and not expired.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("JWT validation failed: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Get the access token expiration duration in milliseconds.
     */
    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    /**
     * Get the refresh token expiration duration in milliseconds.
     */
    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }
}
