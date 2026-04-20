package com.maayn.financialservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import maayn.veld.generated.models.certificate.ApplicationStatus;
import maayn.veld.generated.models.loan.EmploymentStatus;
import maayn.veld.generated.models.shared.InterestType;
import com.maayn.financialservice.domain.loan.LoanType;
import maayn.veld.generated.models.shared.LoanStatus;
import maayn.veld.generated.models.shared.RepaymentFrequency;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "loan_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationDocument {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Indexed
    private UUID customerId;

    @Indexed
    private UUID productId;

    private LoanType loanType;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal requestedAmount;

    private Integer requestedTenure;

    private String purpose;

    private EmploymentStatus employmentStatus;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal annualIncome;

    @Builder.Default
    private ApplicationStatus applicationStatus = ApplicationStatus.SUBMITTED;

    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    private String remarks;

    @Indexed(unique = true)
    private String loanNumber;

    private UUID accountId;

    private UUID applicationId;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal principalAmount;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal interestRate;

    private InterestType interestType;

    private Integer tenureMonths;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal emiAmount;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal outstandingBalance;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal disbursedAmount;

    private RepaymentFrequency repaymentFrequency;

    private LoanStatus loanStatus;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime disbursementDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
