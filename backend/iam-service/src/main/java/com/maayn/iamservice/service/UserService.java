package com.maayn.iamservice.service;

import com.maayn.iamservice.domain.entity.Role;
import com.maayn.iamservice.domain.entity.User;
import com.maayn.iamservice.repository.RoleRepository;
import com.maayn.iamservice.repository.UserRepository;
import com.maayn.iamservice.security.PasswordHashService;
import com.maayn.iamservice.web.AdminUserView;
import maayn.veld.generated.errors.NotFoundException;
import maayn.veld.generated.models.authentication.UserResponse;
import maayn.veld.generated.models.shared.ChangePasswordRequest;
import maayn.veld.generated.models.shared.UpdateUserRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User Management Service
 * Handles user CRUD operations, role assignment, password changes
 */
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

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUser(UUID userId) {
        User user = findUserOrThrow(userId);
        return toResponse(user);
    }

    // ============================================================================
    // Admin / management operations (rich DTO including all roles + flags)
    // ============================================================================

    @Transactional(readOnly = true)
    public List<AdminUserView> listUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getCreatedAt).reversed())
                .map(this::toAdminView)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminUserView getUserAdminView(UUID userId) {
        return toAdminView(findUserOrThrow(userId));
    }

    @Transactional(readOnly = true)
    public List<String> listRoleNames() {
        return roleRepository.findAll().stream()
                .map(Role::getName)
                .sorted()
                .collect(Collectors.toList());
    }

    private AdminUserView toAdminView(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .sorted()
                .collect(Collectors.toList());
        return new AdminUserView(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                roleNames,
                Boolean.TRUE.equals(user.getIsActive()),
                Boolean.TRUE.equals(user.getIsEmailVerified()),
                user.isAccountLocked(),
                Boolean.TRUE.equals(user.getMfaEnabled()),
                user.getCreatedAt(),
                user.getLastLogin()
        );
    }

    /**
     * Update user profile (name, email)
     */
    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = findUserOrThrow(userId);
        user.updateProfile(request.getUserName(), request.getEmail());
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    /**
     * Assign role to user
     * Supports multiple roles through user.addRole()
     */
    @Transactional
    public UserResponse assignRole(UUID userId, String roleName) {
        User user = findUserOrThrow(userId);
        Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleName, "ROLE_NOT_FOUND"));
        
        user.addRole(role);
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    /**
     * Remove role from user
     */
    @Transactional
    public UserResponse removeRole(UUID userId, String roleName) {
        User user = findUserOrThrow(userId);
        Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleName, "ROLE_NOT_FOUND"));
        
        user.removeRole(role);
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = findUserOrThrow(userId);
        
        if (!passwordHashService.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        user.changePassword(passwordHashService.hash(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Lock user account
     */
    @Transactional
    public void lockUser(UUID userId) {
        User user = findUserOrThrow(userId);
        user.lockAccount();
        userRepository.save(user);
    }

    /**
     * Unlock user account
     */
    @Transactional
    public void unlockUser(UUID userId) {
        User user = findUserOrThrow(userId);
        user.unlockAccount();
        userRepository.save(user);
    }

    /**
     * Deactivate user account
     */
    @Transactional
    public void deactivateUser(UUID userId) {
        User user = findUserOrThrow(userId);
        user.deactivate();
        userRepository.save(user);
    }

    /**
     * Activate user account
     */
    @Transactional
    public void activateUser(UUID userId) {
        User user = findUserOrThrow(userId);
        user.activate();
        userRepository.save(user);
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId, "USER_NOT_FOUND"));
    }

    private UserResponse toResponse(User user) {
        // Get primary role
        String primaryRole = user.getRoles().stream()
                .map(r -> r.getName())
                .sorted((a, b) -> {
                    int orderA = getRoleOrder(a);
                    int orderB = getRoleOrder(b);
                    return Integer.compare(orderB, orderA);
                })
                .findFirst()
                .orElse("CUSTOMER");

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                primaryRole
        );
    }

    private int getRoleOrder(String role) {
        return switch (role.toUpperCase()) {
            case "ADMIN" -> 3;
            case "EMPLOYEE" -> 2;
            case "CUSTOMER" -> 1;
            default -> 0;
        };
    }
}
