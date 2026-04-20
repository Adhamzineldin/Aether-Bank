package com.maayn.accountservice.dto;

import com.maayn.accountservice.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountStatusRequest {
    
    @NotNull(message = "Status is required")
    private AccountStatus status;
    
    private String reason; // Optional
}

