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

import java.util.Locale;

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

    @Override
    public JwtResponse login(LoginRequest request) throws LoginException {
        User user = userRepository.findByUserName(request.getUserName())
                .orElseThrow(() -> AuthenticationErrors.login.userNotFound("User not found"));

        if (!passwordHashService.matches(request.getPassword(), user.getPassword())) {
            throw AuthenticationErrors.login.invalidCredentials("Invalid username or password");
        }

        String token = jwtService.generateToken(user);
        return new JwtResponse(token, "Bearer");
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new RuntimeException("Username already exists");
        }

        registrationValidator.validate(request.getEmail(), request.getPassword());

        String requestedRole = request.getRole() == null || request.getRole().isBlank()
                ? "CUSTOMER"
                : request.getRole().trim().toUpperCase(Locale.ROOT);

        Role role = roleRepository.findByName(requestedRole)
                .orElseGet(() -> roleRepository.save(Role.builder().name(requestedRole).build()));

        User user = User.builder()
                .userName(request.getUserName())
                .email(request.getEmail())
                .password(passwordHashService.hash(request.getPassword()))
                .role(role)
                .build();

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getUserName(),
                savedUser.getEmail(),
                savedUser.getRole().getName()
        );
    }

    @Override
    public GenericResponse logout() {
        return new GenericResponse("Logout successful", true);
    }
}