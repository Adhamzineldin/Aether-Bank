package com.maayn.financialservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.index.Indexed;

import maayn.veld.generated.models.loan.ApplicationStatus;
import maayn.veld.generated.models.loan.EmploymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "loan_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    private String id;

    @Indexed
    private UUID customerId;

    @Indexed
    private UUID productId;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal requestedAmount;

    private Integer requestedTenureMonths;

    private String purpose;

    private EmploymentStatus employmentStatus;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal annualIncome;

    @Builder.Default
    private ApplicationStatus applicationStatus = ApplicationStatus.SUBMITTED;

    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    private String remarks;
}