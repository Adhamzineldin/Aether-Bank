package com.maayn.iamservice.mapper;


import com.maayn.iamservice.domain.dto.UserDTO;
import com.maayn.iamservice.domain.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    // Convert Entity → DTO
    public UserDTO toDTO(User user) {

        if (user == null) {
            return null;
        }

        return UserDTO.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .build();
    }
}