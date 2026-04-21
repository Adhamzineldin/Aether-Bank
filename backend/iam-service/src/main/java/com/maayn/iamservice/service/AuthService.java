package com.maayn.iamservice.service;

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

    public AuthService(
            JwtService jwtService,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordHashService passwordHashService,
            RegistrationValidator registrationValidator
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordHashService = passwordHashService;
        this.registrationValidator = registrationValidator;
    }

    /**
     * Authenticate user with username/email and password
     * Validates credentials, checks account lock status, records failed/successful attempts
     */
    @Override
    @Transactional
    public JwtResponse login(LoginRequest request) throws LoginException {
        User user = userRepository.findByUsername(request.getUserName())
                .orElseThrow(() -> AuthenticationErrors.LoginErrors.userNotFound("User not found"));

        // Check if account is locked
        if (user.isAccountLocked()) {
            throw AuthenticationErrors.LoginErrors.invalidCredentials("Account is temporarily locked");
        }

        // Check if user is active
        if (!user.getIsActive()) {
            throw AuthenticationErrors.LoginErrors.invalidCredentials("Account is inactive");
        }

        // Verify password
        if (!passwordHashService.matches(request.getPassword(), user.getPasswordHash())) {
            user.recordFailedLogin();
            userRepository.save(user);
            throw AuthenticationErrors.LoginErrors.invalidCredentials("Invalid username or password");
        }

        // Record successful login
        user.recordSuccessfulLogin();
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new JwtResponse(token, "Bearer");
    }

    /**
     * Register new user account
     * Validates input, hashes password, assigns default CUSTOMER role
     */
    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUserName())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Validate input
        registrationValidator.validate(request.getEmail(), request.getPassword());

        // SECURITY: public self-registration always lands the user in the CUSTOMER
        // role regardless of what the client sends. Elevated roles (ADMIN,
        // SUPERADMIN, EMPLOYEE, ...) can only be granted afterwards by a superadmin
        // via the user-management endpoints.
        final String DEFAULT_ROLE = "CUSTOMER";
        Role role = roleRepository.findByName(DEFAULT_ROLE)
                .orElseGet(() -> roleRepository.save(Role.builder().name(DEFAULT_ROLE).build()));

        // Create user
        User user = User.builder()
                .username(request.getUserName())
                .email(request.getEmail())
                .passwordHash(passwordHashService.hash(request.getPassword()))
                .fullName(request.getUserName())
                .isActive(true)
                .isEmailVerified(false)
                .mfaEnabled(false)
                .build();

        // Add role to user
        user.addRole(role);

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                role.getName()
        );
    }

    /**
     * Logout user (stateless - handled client-side)
     */
    @Override
    public GenericResponse logout() {
        return new GenericResponse("Logout successful", true);
    }
}