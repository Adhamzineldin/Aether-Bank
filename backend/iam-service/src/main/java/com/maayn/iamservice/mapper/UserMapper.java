package com.maayn.iamservice.mapper;


import com.maayn.iamservice.domain.entity.User;
import maayn.veld.generated.models.authentication.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;
        
        // Extract primary role (ADMIN > EMPLOYEE > CUSTOMER)
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