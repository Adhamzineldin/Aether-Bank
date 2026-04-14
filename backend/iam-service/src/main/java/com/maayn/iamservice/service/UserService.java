package com.maayn.iamservice.service;


import com.maayn.iamservice.domain.dto.UserDTO;

import java.util.UUID;

public interface UserService {

    // Update user profile (username/email)
    UserDTO updateUser(UUID userId, String userName, String email);

    // Assign role to user (admin operation)
    UserDTO assignRole(UUID userId, String roleName);
}
