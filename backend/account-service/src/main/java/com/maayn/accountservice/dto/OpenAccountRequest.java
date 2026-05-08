package com.maayn.accountservice.dto;

import com.maayn.accountservice.enums.AccountType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenAccountRequest {
    
    // OCL: BA_13_CustomerIdRequired
    @NotNull(message = "Customer ID is required")
    private UUID customerId;
    
    // OCL: BA_14_AccountTypeRequired
    @NotNull(message = "Account type is required")
    private AccountType accountType;
    
    // OCL: BA_15_RequestCurrencyLooksISO3
    @NotNull(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3-letter code (e.g., USD, EUR)")
    private String currency;
    
    @Positive(message = "Initial deposit must be positive")
    private BigDecimal initialDeposit; // Optional
}

