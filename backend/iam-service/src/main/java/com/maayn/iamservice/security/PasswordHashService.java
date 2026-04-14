package com.maayn.iamservice.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordHashService {

    private final PasswordEncoder passwordEncoder;

    public PasswordHashService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String hash(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean matches(String raw, String hashed) {
        return passwordEncoder.matches(raw, hashed);
    }
}