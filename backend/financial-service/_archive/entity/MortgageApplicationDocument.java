package com.maayn.financialservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import maayn.veld.generated.models.certificate.ApplicationStatus;
import maayn.veld.generated.models.loan.EmploymentStatus;
import maayn.veld.generated.models.shared.MortgageStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "mortgage_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MortgageApplicationDocument {

    @Id
    private UUID id;

    @Indexed
    private UUID customerId;

    @Indexed(unique = true)
    private String mortgageNumber;

    private String propertyAddress;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal propertyValue;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal requestedAmount;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal downPayment;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal interestRate;

    private Integer termYears;

    private EmploymentStatus employmentStatus;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal annualIncome;

    @Builder.Default
    private ApplicationStatus applicationStatus = ApplicationStatus.SUBMITTED;

    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    private String remarks;

    private MortgageStatus mortgageStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
