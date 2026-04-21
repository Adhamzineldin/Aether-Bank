package com.maayn.iamservice.web;

import com.maayn.iamservice.service.UserService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Admin user-management endpoints.
 *
 * Mounted under {@code /api/auth/users} so it lines up with the existing
 * frontend client (which already calls {@code /api/auth/users/...} via the
 * gateway). All endpoints require an authenticated user; method-level
 * {@link PreAuthorize} restricts mutation to SUPERADMIN, with read access
 * shared with ADMIN.
 */
@RestController
@RequestMapping("/api/auth")
@PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
public class UserManagementController {

    private final UserService userService;

    public UserManagementController(UserService userService) {
        this.userService = userService;
    }

    // -------- Listing & lookup (ADMIN + SUPERADMIN) --------

    @GetMapping("/users")
    public List<AdminUserView> listUsers() {
        return userService.listUsers();
    }

    @GetMapping("/users/{id}")
    public AdminUserView getUser(@PathVariable UUID id) {
        return userService.getUserAdminView(id);
    }

    @GetMapping("/roles")
    public List<String> listRoles() {
        return userService.listRoleNames();
    }

    // -------- Role management (SUPERADMIN only) --------

    public record RoleAssignmentRequest(@NotBlank String role) { }

    @PostMapping("/users/{id}/roles")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public AdminUserView assignRole(@PathVariable UUID id,
                                    @RequestBody RoleAssignmentRequest body) {
        userService.assignRole(id, body.role());
        return userService.getUserAdminView(id);
    }

    @DeleteMapping("/users/{id}/roles/{role}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public AdminUserView removeRole(@PathVariable UUID id,
                                    @PathVariable String role) {
        userService.removeRole(id, role);
        return userService.getUserAdminView(id);
    }

    // -------- Account state (SUPERADMIN only) --------

    @PostMapping("/users/{id}/lock")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<AdminUserView> lock(@PathVariable UUID id) {
        userService.lockUser(id);
        return ResponseEntity.ok(userService.getUserAdminView(id));
    }

    @PostMapping("/users/{id}/unlock")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<AdminUserView> unlock(@PathVariable UUID id) {
        userService.unlockUser(id);
        return ResponseEntity.ok(userService.getUserAdminView(id));
    }

    @PostMapping("/users/{id}/activate")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<AdminUserView> activate(@PathVariable UUID id) {
        userService.activateUser(id);
        return ResponseEntity.ok(userService.getUserAdminView(id));
    }

    @PostMapping("/users/{id}/deactivate")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<AdminUserView> deactivate(@PathVariable UUID id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(userService.getUserAdminView(id));
    }
}

