package com.maayn.iamservice.service;

import com.maayn.iamservice.audit.AuditPublisher;
import com.maayn.iamservice.domain.entity.Role;
import com.maayn.iamservice.domain.entity.User;
import com.maayn.iamservice.repository.RoleRepository;
import com.maayn.iamservice.repository.UserRepository;
import com.maayn.iamservice.security.PasswordHashService;
import com.maayn.iamservice.validator.RegistrationValidator;
import maayn.veld.generated.errors.AuthenticationErrors;
import maayn.veld.generated.errors.LoginException;
import maayn.veld.generated.models.authentication.GenericResponse;
import maayn.veld.generated.models.authentication.JwtResponse;
import maayn.veld.generated.models.authentication.LoginRequest;
import maayn.veld.generated.models.authentication.RegisterRequest;
import maayn.veld.generated.models.authentication.UserResponse;
import maayn.veld.generated.services.IAuthenticationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Authentication Service
 * Handles user login, registration, and token generation
 */
@Service
public class AuthService implements IAuthenticationService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordHashService passwordHashService;
    private final RegistrationValidator registrationValidator;
    private final AuditPublisher auditPublisher;

    public AuthService(
            JwtService jwtService,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordHashService passwordHashService,
            RegistrationValidator registrationValidator,
            AuditPublisher auditPublisher
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordHashService = passwordHashService;
        this.registrationValidator = registrationValidator;
        this.auditPublisher = auditPublisher;
    }

    /**
     * Authenticate user with username/email and password
     * Validates credentials, checks account lock status, records failed/successful attempts
     */
    @Override
    @Transactional
    public JwtResponse login(LoginRequest request) throws LoginException {
        User user;
        try {
            user = userRepository.findByUsername(request.getUserName())
                    .orElseThrow(() -> AuthenticationErrors.LoginErrors.userNotFound("User not found"));
        } catch (LoginException ex) {
            auditPublisher.publishFailure("LOGIN_ATTEMPT", null,
                    "Unknown user: " + request.getUserName());
            throw ex;
        }

        if (user.isAccountLocked()) {
            auditPublisher.publishFailure("LOGIN_ATTEMPT", user.getId(),
                    "Account locked: " + user.getUsername());
            throw AuthenticationErrors.LoginErrors.invalidCredentials("Account is temporarily locked");
        }

        if (!user.getIsActive()) {
            auditPublisher.publishFailure("LOGIN_ATTEMPT", user.getId(),
                    "Account inactive: " + user.getUsername());
            throw AuthenticationErrors.LoginErrors.invalidCredentials("Account is inactive");
        }

        if (!passwordHashService.matches(request.getPassword(), user.getPasswordHash())) {
            user.recordFailedLogin();
            userRepository.save(user);
            auditPublisher.publishFailure("LOGIN_ATTEMPT", user.getId(),
                    "Invalid password for: " + user.getUsername());
            throw AuthenticationErrors.LoginErrors.invalidCredentials("Invalid username or password");
        }

        user.recordSuccessfulLogin();
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        auditPublisher.publishSuccess("LOGIN_ATTEMPT", user.getId(),
                "Logged in: " + user.getUsername());
        return new JwtResponse(token, "Bearer");
    }

    /**
     * Register new user account
     * Validates input, hashes password, assigns default CUSTOMER role
     */
    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        try {
            if (userRepository.existsByUsername(request.getUserName())) {
                throw new RuntimeException("Username already exists");
            }
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            registrationValidator.validate(request.getEmail(), request.getPassword());

            final String DEFAULT_ROLE = "CUSTOMER";
            Role role = roleRepository.findByName(DEFAULT_ROLE)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(DEFAULT_ROLE).build()));

            User user = User.builder()
                    .username(request.getUserName())
                    .email(request.getEmail())
                    .passwordHash(passwordHashService.hash(request.getPassword()))
                    .fullName(request.getUserName())
                    .isActive(true)
                    .isEmailVerified(false)
                    .mfaEnabled(false)
                    .build();
            user.addRole(role);

            User savedUser = userRepository.save(user);

            auditPublisher.publishSuccess("REGISTER_USER", savedUser.getId(),
                    String.format("Registered user %s (%s) role=%s",
                            savedUser.getUsername(), savedUser.getEmail(), role.getName()));

            return new UserResponse(
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    role.getName()
            );
        } catch (RuntimeException ex) {
            auditPublisher.publishFailure("REGISTER_USER", null,
                    String.format("Registration failed for %s: %s",
                            request.getUserName(), ex.getMessage()));
            throw ex;
        }
    }

    /**
     * Logout user (stateless - handled client-side)
     */
    @Override
    public GenericResponse logout() {
        auditPublisher.publishSuccess("LOGOUT", null, "User logout");
        return new GenericResponse("Logout successful", true);
    }
}