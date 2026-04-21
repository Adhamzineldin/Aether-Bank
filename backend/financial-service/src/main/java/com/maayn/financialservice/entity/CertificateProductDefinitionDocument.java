package com.maayn.financialservice.entity;

import com.maayn.financialservice.domain.certificate.CertificateInterestMethod;
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
import java.util.UUID;

@Document(collection = "certificate_product_definitions")
@Getter
@Setter
@NoArgsConstructor
public class CertificateProductDefinitionDocument {

    @Id
    private UUID id;

    @Indexed(unique = true)
    private String code;

    private String name;

    private CertificateInterestMethod interestMethod;

    private PayoutMethod payoutMethod;

    private LiquidityMethod liquidityMethod;

    private RateBehaviorMethod rateBehaviorMethod;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal baseAnnualRate;

    private Integer payoutIntervalDays;

    private Integer termDays;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal penaltyRate;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal minimumPrincipal;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal maximumPrincipal;

    private String indexCode;

    private Boolean active = Boolean.TRUE;
}
