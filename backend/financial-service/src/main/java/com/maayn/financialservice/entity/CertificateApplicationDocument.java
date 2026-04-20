package com.maayn.financialservice.entity;

import com.maayn.financialservice.model.CertificateStatus;
import lombok.*;
import maayn.veld.generated.models.loan.ApplicationStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "certificate_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateApplicationDocument {

    @Id
    private UUID id;

    @Indexed
    private UUID customerId;

    @Indexed
    private UUID accountId;

    @Indexed(unique = true)
    private String certificateNumber;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal principal;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal interestRate;

    private Integer termDays;
    private Boolean autoRenew;

    @Builder.Default
    private ApplicationStatus applicationStatus = ApplicationStatus.SUBMITTED;

    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String remarks;
    private LocalDateTime openDate;
    private LocalDateTime maturityDate;
    private CertificateStatus certificateStatus;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal interestEarned;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
