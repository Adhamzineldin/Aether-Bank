package com.maayn.financialservice.entity;

import com.maayn.financialservice.domain.loan.LoanInterestMethod;
import com.maayn.financialservice.domain.loan.LoanRateMode;
import com.maayn.financialservice.domain.loan.LoanRepaymentMethod;
import com.maayn.financialservice.domain.loan.LoanType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.util.UUID;

@Document(collection = "loan_product_definitions")
public class LoanProductDefinitionDocument {

    @Id
    private UUID id;

    @Indexed(unique = true)
    private String code;

    private String name;

    private LoanType loanType;

    private LoanInterestMethod interestMethod;

    private LoanRateMode rateMode;

    private LoanRepaymentMethod repaymentMethod;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal baseAnnualRate;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal monthlyFee;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal penaltyRate;

    private Integer defaultTenureMonths;

    private Integer minimumTenureMonths;

    private Integer maximumTenureMonths;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal minimumPrincipal;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal maximumPrincipal;

    private String variableRateIndexCode;

    private Boolean active = Boolean.TRUE;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LoanType getLoanType() { return loanType; }
    public void setLoanType(LoanType loanType) { this.loanType = loanType; }
    public LoanInterestMethod getInterestMethod() { return interestMethod; }
    public void setInterestMethod(LoanInterestMethod interestMethod) { this.interestMethod = interestMethod; }
    public LoanRateMode getRateMode() { return rateMode; }
    public void setRateMode(LoanRateMode rateMode) { this.rateMode = rateMode; }
    public LoanRepaymentMethod getRepaymentMethod() { return repaymentMethod; }
    public void setRepaymentMethod(LoanRepaymentMethod repaymentMethod) { this.repaymentMethod = repaymentMethod; }
    public BigDecimal getBaseAnnualRate() { return baseAnnualRate; }
    public void setBaseAnnualRate(BigDecimal baseAnnualRate) { this.baseAnnualRate = baseAnnualRate; }
    public BigDecimal getMonthlyFee() { return monthlyFee; }
    public void setMonthlyFee(BigDecimal monthlyFee) { this.monthlyFee = monthlyFee; }
    public BigDecimal getPenaltyRate() { return penaltyRate; }
    public void setPenaltyRate(BigDecimal penaltyRate) { this.penaltyRate = penaltyRate; }
    public Integer getDefaultTenureMonths() { return defaultTenureMonths; }
    public void setDefaultTenureMonths(Integer defaultTenureMonths) { this.defaultTenureMonths = defaultTenureMonths; }
    public Integer getMinimumTenureMonths() { return minimumTenureMonths; }
    public void setMinimumTenureMonths(Integer minimumTenureMonths) { this.minimumTenureMonths = minimumTenureMonths; }
    public Integer getMaximumTenureMonths() { return maximumTenureMonths; }
    public void setMaximumTenureMonths(Integer maximumTenureMonths) { this.maximumTenureMonths = maximumTenureMonths; }
    public BigDecimal getMinimumPrincipal() { return minimumPrincipal; }
    public void setMinimumPrincipal(BigDecimal minimumPrincipal) { this.minimumPrincipal = minimumPrincipal; }
    public BigDecimal getMaximumPrincipal() { return maximumPrincipal; }
    public void setMaximumPrincipal(BigDecimal maximumPrincipal) { this.maximumPrincipal = maximumPrincipal; }
    public String getVariableRateIndexCode() { return variableRateIndexCode; }
    public void setVariableRateIndexCode(String variableRateIndexCode) { this.variableRateIndexCode = variableRateIndexCode; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
