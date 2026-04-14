package com.maayn.iamservice.validator;

import com.maayn.iamservice.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class RegistrationValidator {

    private final UserRepository userRepository;

    public RegistrationValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Validate registration request
    public void validate(String email, String password) {

        validateEmailUniqueness(email);
        validatePasswordStrength(password);
    }

    // Check if email already exists
    private void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
    }

    // Check password strength rules
    private void validatePasswordStrength(String password) {

        if (password == null || password.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new RuntimeException("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[0-9].*")) {
            throw new RuntimeException("Password must contain at least one number");
        }
    }
}