package com.maayn.financialservice.validation;

import maayn.veld.generated.models.mortgage.MortgageApplication;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MortgageValidator {

    public void validateSubmission(MortgageApplication application) {
        if (application.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }

        if (application.getPropertyAddress() == null || application.getPropertyAddress().isBlank()) {
            throw new IllegalArgumentException("Property address is required");
        }

        if (application.getPropertyValue() == null || application.getPropertyValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valid property value is required");
        }

        if (application.getDownPayment() == null || application.getDownPayment().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valid down payment is required");
        }

        if (application.getRequestedAmount() == null || application.getRequestedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valid loan amount is required");
        }

        if (application.getTermYears() <= 0 || application.getTermYears() > 30) {
            throw new IllegalArgumentException("Mortgage term must be between 1 and 30 years");
        }

        if (application.getAnnualIncome() == null || application.getAnnualIncome().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valid annual income is required");
        }

        // Validate down payment is at least 20% of property value
        BigDecimal minDownPayment = application.getPropertyValue().multiply(new BigDecimal("0.20"));
        if (application.getDownPayment().compareTo(minDownPayment) < 0) {
            throw new IllegalArgumentException("Down payment must be at least 20% of property value");
        }

        // Validate credit score if provided
        if (application.getCreditScore() != null && 
            (application.getCreditScore() < 300 || application.getCreditScore() > 850)) {
            throw new IllegalArgumentException("Credit score must be between 300 and 850");
        }
    }
}

