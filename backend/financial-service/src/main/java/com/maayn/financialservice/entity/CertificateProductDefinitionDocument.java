package com.maayn.financialservice.entity;

import com.maayn.financialservice.domain.certificate.CertificateInterestMethod;
import com.maayn.financialservice.domain.certificate.LiquidityMethod;
import com.maayn.financialservice.domain.certificate.PayoutMethod;
import com.maayn.financialservice.domain.certificate.RateBehaviorMethod;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.util.UUID;

@Document(collection = "certificate_product_definitions")
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

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public CertificateInterestMethod getInterestMethod() { return interestMethod; }
    public void setInterestMethod(CertificateInterestMethod interestMethod) { this.interestMethod = interestMethod; }
    public PayoutMethod getPayoutMethod() { return payoutMethod; }
    public void setPayoutMethod(PayoutMethod payoutMethod) { this.payoutMethod = payoutMethod; }
    public LiquidityMethod getLiquidityMethod() { return liquidityMethod; }
    public void setLiquidityMethod(LiquidityMethod liquidityMethod) { this.liquidityMethod = liquidityMethod; }
    public RateBehaviorMethod getRateBehaviorMethod() { return rateBehaviorMethod; }
    public void setRateBehaviorMethod(RateBehaviorMethod rateBehaviorMethod) { this.rateBehaviorMethod = rateBehaviorMethod; }
    public BigDecimal getBaseAnnualRate() { return baseAnnualRate; }
    public void setBaseAnnualRate(BigDecimal baseAnnualRate) { this.baseAnnualRate = baseAnnualRate; }
    public Integer getPayoutIntervalDays() { return payoutIntervalDays; }
    public void setPayoutIntervalDays(Integer payoutIntervalDays) { this.payoutIntervalDays = payoutIntervalDays; }
    public Integer getTermDays() { return termDays; }
    public void setTermDays(Integer termDays) { this.termDays = termDays; }
    public BigDecimal getPenaltyRate() { return penaltyRate; }
    public void setPenaltyRate(BigDecimal penaltyRate) { this.penaltyRate = penaltyRate; }
    public BigDecimal getMinimumPrincipal() { return minimumPrincipal; }
    public void setMinimumPrincipal(BigDecimal minimumPrincipal) { this.minimumPrincipal = minimumPrincipal; }
    public BigDecimal getMaximumPrincipal() { return maximumPrincipal; }
    public void setMaximumPrincipal(BigDecimal maximumPrincipal) { this.maximumPrincipal = maximumPrincipal; }
    public String getIndexCode() { return indexCode; }
    public void setIndexCode(String indexCode) { this.indexCode = indexCode; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
