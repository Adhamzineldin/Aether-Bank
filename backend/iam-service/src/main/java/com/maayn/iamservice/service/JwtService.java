package com.maayn.iamservice.service;

import com.maayn.iamservice.config.JwtProperties;
import com.maayn.iamservice.domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JWT Token Service
 * Handles generation, validation, and parsing of JWT tokens
 * Uses HMAC-SHA256 for signing
 */
@Service
public class JwtService {
    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate JWT token for authenticated user
     * Token includes user ID, username, roles, and permissions
     * Expiration: configurable (default 15 minutes for access token)
     */
    public String generateToken(User user) {
        String roles = user.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(user.getId().toString())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(jwtProperties.getExpirationMs())))
                .claims(Map.of(
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "roles", roles
                ))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Generate JWT token with custom expiration
     * Used for refresh tokens with longer expiration
     */
    public String generateToken(String username, String roles, long expirationMs) {
        return Jwts.builder()
                .subject(username)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(expirationMs)))
                .claims(Map.of("roles", roles))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Validate JWT token
     * Checks signature and expiration
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract username from JWT claims
     */
    public String extractUsername(String token) {
        Claims claims = parseClaims(token);
        String username = claims.get("username", String.class);
        return username != null ? username : claims.getSubject();
    }

    /**
     * Extract user ID (subject) from JWT claims
     */
    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extract roles from JWT claims
     */
    public String extractRoles(String token) {
        Claims claims = parseClaims(token);
        return claims.get("roles", String.class);
    }

    /**
     * Parse and verify JWT claims
     * Throws exception if token is invalid or expired
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
