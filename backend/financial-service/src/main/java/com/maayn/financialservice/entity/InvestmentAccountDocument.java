package com.maayn.financialservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "investment_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentAccountDocument {
    
    @Id
    private UUID id;
    
    private String accountNumber;
    
    private UUID customerId;
    
    private UUID linkedAccountId; // Bank account for funding
    
    private String status; // PENDING, ACTIVE, SUSPENDED, CLOSED
    
    private BigDecimal totalValue;
    
    private String currency;
    
    private LocalDateTime openedDate;
    
    private LocalDateTime closedDate;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}

