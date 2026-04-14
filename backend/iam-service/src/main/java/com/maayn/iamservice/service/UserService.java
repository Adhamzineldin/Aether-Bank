package com.maayn.iamservice.service;

import com.maayn.iamservice.domain.entity.Role;
import com.maayn.iamservice.domain.entity.User;
import com.maayn.iamservice.repository.RoleRepository;
import com.maayn.iamservice.repository.UserRepository;
import com.maayn.iamservice.security.PasswordHashService;
import maayn.veld.generated.errors.NotFoundException;
import maayn.veld.generated.models.authentication.UserResponse;
import maayn.veld.generated.models.shared.ChangePasswordRequest;
import maayn.veld.generated.models.shared.UpdateUserRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordHashService passwordHashService;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordHashService passwordHashService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordHashService = passwordHashService;
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = findUserOrThrow(userId);
        user.updateProfile(request.getUserName(), request.getEmail());
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Transactional
    public UserResponse assignRole(UUID userId, String roleName) {
        User user = findUserOrThrow(userId);
        Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElseGet(() -> roleRepository.save(Role.builder().name(roleName.toUpperCase()).build()));
        user.assignRole(role);
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = findUserOrThrow(userId);
        if (!passwordHashService.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.changePassword(passwordHashService.hash(request.getNewPassword()));
        userRepository.save(user);
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId, "USER_NOT_FOUND"));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getRole().getName()
        );
    }
}
