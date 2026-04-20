package com.maayn.financialservice.entity;

import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RateChangeDocument {

    private LocalDate effectiveDate;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal annualRate;

    private String source;

    private String notes;

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public BigDecimal getAnnualRate() { return annualRate; }
    public void setAnnualRate(BigDecimal annualRate) { this.annualRate = annualRate; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
