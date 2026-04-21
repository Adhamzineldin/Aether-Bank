package com.maayn.iamservice.web;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Richer user representation returned by the admin/user-management endpoints.
 * Distinct from the auto-generated UserResponse (which only carries a single
 * primary role) so the admin UI can render the full role set and account flags.
 */
public record AdminUserView(
        UUID id,
        String username,
        String email,
        String fullName,
        List<String> roles,
        boolean isActive,
        boolean isEmailVerified,
        boolean isLocked,
        boolean mfaEnabled,
        LocalDateTime createdAt,
        LocalDateTime lastLogin
) { }

