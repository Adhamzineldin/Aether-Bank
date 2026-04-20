package com.maayn.accountservice.dto;

import com.maayn.accountservice.enums.AccountStatus;
import com.maayn.accountservice.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private UUID id;
    private String accountNumber;
    private UUID customerId;
    private AccountType accountType;
    private AccountStatus status;
    private String currency;
    private LocalDate openedDate;
    private LocalDate closedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal balance; // From Transaction Service
}

