package com.maayn.financialservice.entity;

import com.maayn.financialservice.domain.loan.LoanInterestMethod;
import com.maayn.financialservice.domain.loan.LoanLifecycleStatus;
import com.maayn.financialservice.domain.loan.LoanRateMode;
import com.maayn.financialservice.domain.loan.LoanRepaymentMethod;
import com.maayn.financialservice.domain.loan.LoanType;
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

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public String getLoanNumber() { return loanNumber; }
    public void setLoanNumber(String loanNumber) { this.loanNumber = loanNumber; }
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public LoanType getLoanType() { return loanType; }
    public void setLoanType(LoanType loanType) { this.loanType = loanType; }
    public BigDecimal getPrincipal() { return principal; }
    public void setPrincipal(BigDecimal principal) { this.principal = principal; }
    public BigDecimal getOutstandingPrincipal() { return outstandingPrincipal; }
    public void setOutstandingPrincipal(BigDecimal outstandingPrincipal) { this.outstandingPrincipal = outstandingPrincipal; }
    public BigDecimal getAccruedInterest() { return accruedInterest; }
    public void setAccruedInterest(BigDecimal accruedInterest) { this.accruedInterest = accruedInterest; }
    public BigDecimal getAnnualRate() { return annualRate; }
    public void setAnnualRate(BigDecimal annualRate) { this.annualRate = annualRate; }
    public LoanInterestMethod getInterestMethod() { return interestMethod; }
    public void setInterestMethod(LoanInterestMethod interestMethod) { this.interestMethod = interestMethod; }
    public LoanRateMode getRateMode() { return rateMode; }
    public void setRateMode(LoanRateMode rateMode) { this.rateMode = rateMode; }
    public LoanRepaymentMethod getRepaymentMethod() { return repaymentMethod; }
    public void setRepaymentMethod(LoanRepaymentMethod repaymentMethod) { this.repaymentMethod = repaymentMethod; }
    public BigDecimal getMonthlyFee() { return monthlyFee; }
    public void setMonthlyFee(BigDecimal monthlyFee) { this.monthlyFee = monthlyFee; }
    public BigDecimal getPenaltyRate() { return penaltyRate; }
    public void setPenaltyRate(BigDecimal penaltyRate) { this.penaltyRate = penaltyRate; }
    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getMaturityDate() { return maturityDate; }
    public void setMaturityDate(LocalDate maturityDate) { this.maturityDate = maturityDate; }
    public LocalDate getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(LocalDate nextDueDate) { this.nextDueDate = nextDueDate; }
    public LoanLifecycleStatus getStatus() { return status; }
    public void setStatus(LoanLifecycleStatus status) { this.status = status; }
    public List<LoanScheduleLineDocument> getScheduleLines() { return scheduleLines; }
    public void setScheduleLines(List<LoanScheduleLineDocument> scheduleLines) { this.scheduleLines = scheduleLines; }
    public List<RateChangeDocument> getRateHistory() { return rateHistory; }
    public void setRateHistory(List<RateChangeDocument> rateHistory) { this.rateHistory = rateHistory; }
    public LocalDate getLastAccruedDate() { return lastAccruedDate; }
    public void setLastAccruedDate(LocalDate lastAccruedDate) { this.lastAccruedDate = lastAccruedDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
