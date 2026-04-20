package com.maayn.accountservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloseAccountRequest {
    
    @NotBlank(message = "Reason is required")
    private String reason;
    
    private UUID transferToAccountId; // Optional: transfer remaining balance here
}

