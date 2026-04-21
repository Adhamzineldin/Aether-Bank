package com.maayn.financialservice.entity;

import com.maayn.financialservice.domain.loan.LoanInterestMethod;
import com.maayn.financialservice.domain.loan.LoanLifecycleStatus;
import com.maayn.financialservice.domain.loan.LoanRateMode;
import com.maayn.financialservice.domain.loan.LoanRepaymentMethod;
import com.maayn.financialservice.domain.loan.LoanType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Document(collection = "loan_accounts")
@Getter
@Setter
@NoArgsConstructor
public class LoanAccountDocument {

    @Id
    private UUID id;

    @Indexed
    private UUID customerId;

    @Indexed(unique = true)
    private String loanNumber;

    @Indexed
    private UUID applicationId;

    @Indexed
    private String productCode;

    private LoanType loanType;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal principal;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal outstandingPrincipal;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal accruedInterest;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal annualRate;

    private LoanInterestMethod interestMethod;

    private LoanRateMode rateMode;

    private LoanRepaymentMethod repaymentMethod;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal monthlyFee;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal penaltyRate;

    private Integer tenureMonths;

    private LocalDate startDate;

    private LocalDate maturityDate;

    private LocalDate nextDueDate;

    private LoanLifecycleStatus status;

    private List<LoanScheduleLineDocument> scheduleLines;

    private List<RateChangeDocument> rateHistory;

    private LocalDate lastAccruedDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
