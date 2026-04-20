package com.maayn.financialservice.entity;

import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanScheduleLineDocument {

    private LocalDate dueDate;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal openingBalance;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal expectedPrincipal;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal expectedInterest;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal expectedFee;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal expectedTotal;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal closingBalance;

    private String status;

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance; }
    public BigDecimal getExpectedPrincipal() { return expectedPrincipal; }
    public void setExpectedPrincipal(BigDecimal expectedPrincipal) { this.expectedPrincipal = expectedPrincipal; }
    public BigDecimal getExpectedInterest() { return expectedInterest; }
    public void setExpectedInterest(BigDecimal expectedInterest) { this.expectedInterest = expectedInterest; }
    public BigDecimal getExpectedFee() { return expectedFee; }
    public void setExpectedFee(BigDecimal expectedFee) { this.expectedFee = expectedFee; }
    public BigDecimal getExpectedTotal() { return expectedTotal; }
    public void setExpectedTotal(BigDecimal expectedTotal) { this.expectedTotal = expectedTotal; }
    public BigDecimal getClosingBalance() { return closingBalance; }
    public void setClosingBalance(BigDecimal closingBalance) { this.closingBalance = closingBalance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
