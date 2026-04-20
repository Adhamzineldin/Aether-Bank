package com.maayn.financialservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import maayn.veld.generated.models.certificate.ApplicationStatus;
import maayn.veld.generated.models.shared.EmploymentStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "mortgage_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MortgageApplicationDocument {
    
    @Id
    private UUID id;
    
    private UUID customerId;
    
    private String propertyAddress;
    
    private BigDecimal propertyValue;
    
    private BigDecimal downPayment;
    
    private BigDecimal requestedAmount;
    
    private int termYears;
    
    private EmploymentStatus employmentStatus;
    
    private BigDecimal annualIncome;
    
    private Integer creditScore;
    
    private ApplicationStatus applicationStatus;
    
    private LocalDateTime submittedAt;
    
    private LocalDateTime reviewedAt;
    
    private String remarks;
    
    // Mortgage details (after approval)
    private String mortgageNumber;
    
    private UUID accountId;
    
    private BigDecimal principalAmount;
    
    private BigDecimal interestRate;
    
    private int termMonths;
    
    private BigDecimal monthlyPayment;
    
    private BigDecimal outstandingBalance;
    
    private String mortgageStatus; // PENDING, APPROVED, REJECTED, DISBURSED, ACTIVE, etc.
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private BigDecimal disbursedAmount;
    
    private LocalDateTime disbursementDate;
    
    private String currency;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}

