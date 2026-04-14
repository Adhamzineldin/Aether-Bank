package com.maayn.iamservice.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserDTO {

    private UUID id;
    private String userName;
    private String email;
    private String role;
}