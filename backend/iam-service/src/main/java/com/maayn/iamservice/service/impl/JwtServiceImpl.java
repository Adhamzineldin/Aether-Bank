package com.maayn.iamservice.service.impl;

import com.maayn.iamservice.config.JwtProperties;
import com.maayn.iamservice.domain.entity.User;
import com.maayn.iamservice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtServiceImpl implements JwtService {
    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtServiceImpl(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(jwtProperties.getExpirationMs())))
                .claims(Map.of(
                        "username", user.getUserName(),
                        "role", user.getRole().getName()
                ))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(jwtProperties.getExpirationMs())))
                .claims(Map.of("role", role))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public String extractUsername(String token) {
        Claims claims = parseClaims(token);
        String username = claims.get("username", String.class);
        return username != null ? username : claims.getSubject();
    }

    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
