package com.maayn.iamservice.mapper;


import com.maayn.iamservice.domain.entity.User;
import maayn.veld.generated.models.authentication.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;
        return new UserResponse(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getRole().getName()
        );
    }
}