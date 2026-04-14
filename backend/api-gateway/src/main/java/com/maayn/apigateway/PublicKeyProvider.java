package com.maayn.apigateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;


@Component
@Slf4j
public class PublicKeyProvider implements ApplicationRunner {

    private final String jwtSecret;
    private SecretKey jwtSigningKey;

    public PublicKeyProvider(@Value("${security.jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    @Override
    public void run(ApplicationArguments args) {
        this.jwtSigningKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        log.info("JWT signing key loaded for gateway verification.");
    }

    public SecretKey getJwtSigningKey() {
        if (jwtSigningKey == null) {
            throw new IllegalStateException("JWT signing key is not loaded.");
        }
        return jwtSigningKey;
    }
}
