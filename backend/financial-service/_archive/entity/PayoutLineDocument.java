package com.maayn.financialservice.entity;

import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PayoutLineDocument {

    private LocalDate dueDate;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal principalReturn;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal interestAmount;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal totalAmount;

    private String status;

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public BigDecimal getPrincipalReturn() { return principalReturn; }
    public void setPrincipalReturn(BigDecimal principalReturn) { this.principalReturn = principalReturn; }
    public BigDecimal getInterestAmount() { return interestAmount; }
    public void setInterestAmount(BigDecimal interestAmount) { this.interestAmount = interestAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
