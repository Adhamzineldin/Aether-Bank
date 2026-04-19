package com.maayn.financialservice.entity;

import com.maayn.financialservice.domain.certificate.CertificateInterestMethod;
import com.maayn.financialservice.domain.certificate.CertificateLifecycleStatus;
import com.maayn.financialservice.domain.certificate.LiquidityMethod;
import com.maayn.financialservice.domain.certificate.PayoutMethod;
import com.maayn.financialservice.domain.certificate.RateBehaviorMethod;
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

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public String getCertificateNumber() { return certificateNumber; }
    public void setCertificateNumber(String certificateNumber) { this.certificateNumber = certificateNumber; }
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public BigDecimal getPrincipal() { return principal; }
    public void setPrincipal(BigDecimal principal) { this.principal = principal; }
    public BigDecimal getAccruedInterest() { return accruedInterest; }
    public void setAccruedInterest(BigDecimal accruedInterest) { this.accruedInterest = accruedInterest; }
    public BigDecimal getAnnualRate() { return annualRate; }
    public void setAnnualRate(BigDecimal annualRate) { this.annualRate = annualRate; }
    public CertificateInterestMethod getInterestMethod() { return interestMethod; }
    public void setInterestMethod(CertificateInterestMethod interestMethod) { this.interestMethod = interestMethod; }
    public PayoutMethod getPayoutMethod() { return payoutMethod; }
    public void setPayoutMethod(PayoutMethod payoutMethod) { this.payoutMethod = payoutMethod; }
    public LiquidityMethod getLiquidityMethod() { return liquidityMethod; }
    public void setLiquidityMethod(LiquidityMethod liquidityMethod) { this.liquidityMethod = liquidityMethod; }
    public RateBehaviorMethod getRateBehaviorMethod() { return rateBehaviorMethod; }
    public void setRateBehaviorMethod(RateBehaviorMethod rateBehaviorMethod) { this.rateBehaviorMethod = rateBehaviorMethod; }
    public BigDecimal getPenaltyRate() { return penaltyRate; }
    public void setPenaltyRate(BigDecimal penaltyRate) { this.penaltyRate = penaltyRate; }
    public Integer getTermDays() { return termDays; }
    public void setTermDays(Integer termDays) { this.termDays = termDays; }
    public Integer getPayoutIntervalDays() { return payoutIntervalDays; }
    public void setPayoutIntervalDays(Integer payoutIntervalDays) { this.payoutIntervalDays = payoutIntervalDays; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public LocalDate getMaturityDate() { return maturityDate; }
    public void setMaturityDate(LocalDate maturityDate) { this.maturityDate = maturityDate; }
    public LocalDate getLastAccruedDate() { return lastAccruedDate; }
    public void setLastAccruedDate(LocalDate lastAccruedDate) { this.lastAccruedDate = lastAccruedDate; }
    public CertificateLifecycleStatus getStatus() { return status; }
    public void setStatus(CertificateLifecycleStatus status) { this.status = status; }
    public List<PayoutLineDocument> getPayoutLines() { return payoutLines; }
    public void setPayoutLines(List<PayoutLineDocument> payoutLines) { this.payoutLines = payoutLines; }
    public List<RateChangeDocument> getRateHistory() { return rateHistory; }
    public void setRateHistory(List<RateChangeDocument> rateHistory) { this.rateHistory = rateHistory; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
