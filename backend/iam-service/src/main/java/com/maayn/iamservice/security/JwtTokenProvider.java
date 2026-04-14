package com.maayn.iamservice.security;

import com.maayn.iamservice.domain.entity.User;
import com.maayn.iamservice.service.impl.JwtServiceImpl;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private final JwtServiceImpl jwtService;

    public JwtTokenProvider(JwtServiceImpl jwtService) {
        this.jwtService = jwtService;
    }

    public String generate(User user) {
        return jwtService.generateToken(user);
    }

    public boolean validate(String token) {
        return jwtService.validateToken(token);
    }

    public String extractUsername(String token) {
        return jwtService.extractUsername(token);
    }
}
