package com.maayn.financialservice.entity;

import com.maayn.financialservice.domain.certificate.CertificateInterestMethod;
import com.maayn.financialservice.domain.certificate.CertificateLifecycleStatus;
import com.maayn.financialservice.domain.certificate.LiquidityMethod;
import com.maayn.financialservice.domain.certificate.PayoutMethod;
import com.maayn.financialservice.domain.certificate.RateBehaviorMethod;
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

@Document(collection = "certificate_accounts")
@Getter
@Setter
@NoArgsConstructor
public class CertificateAccountDocument {

    @Id
    private UUID id;

    @Indexed
    private UUID customerId;

    @Indexed(unique = true)
    private String certificateNumber;

    @Indexed
    private UUID applicationId;

    @Indexed
    private String productCode;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal principal;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal accruedInterest;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal annualRate;

    private CertificateInterestMethod interestMethod;

    private PayoutMethod payoutMethod;

    private LiquidityMethod liquidityMethod;

    private RateBehaviorMethod rateBehaviorMethod;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal penaltyRate;

    private Integer termDays;

    private Integer payoutIntervalDays;

    private LocalDate issueDate;

    private LocalDate maturityDate;

    private LocalDate lastAccruedDate;

    private CertificateLifecycleStatus status;

    private List<PayoutLineDocument> payoutLines;

    private List<RateChangeDocument> rateHistory;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
