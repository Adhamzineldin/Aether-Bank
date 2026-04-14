package com.maayn.iamservice.service;

import com.maayn.iamservice.domain.entity.User;

public interface JwtService {

    String generateToken(User user);

    boolean validateToken(String token);

    // Generate JWT token with username and role
    String generateToken(String username, String role);

    String extractUsername(String token);
}