package com.maayn.financialservice.validation;

import maayn.veld.generated.errors.mortgageErrors;
import maayn.veld.generated.models.mortgage.MortgageApplication;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MortgageValidator {

    public void validateSubmission(MortgageApplication application) {
        validateCustomer(application);
        validatePropertyAddress(application);
        validatePositiveAmount(application);
        validateTerm(application);
        validateIncome(application);
        validateEmploymentStatus(application);
    }

    private void validateCustomer(MortgageApplication application) {
        if (application.getCustomerId() == null) {
            throw mortgageErrors.mortgageSubmit.missingField("Customer ID is required");
        }
    }

    private void validatePropertyAddress(MortgageApplication application) {
        if (application.getPropertyAddress() == null || application.getPropertyAddress().trim().isEmpty()) {
            throw mortgageErrors.mortgageSubmit.missingField("Property address is required");
        }
    }

    private void validatePositiveAmount(MortgageApplication application) {
        if (application.getRequestedAmount() == null || application.getRequestedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw mortgageErrors.mortgageSubmit.invalidAmount("Requested amount must be greater than 0");
        }
        if (application.getPropertyValue() == null || application.getPropertyValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw mortgageErrors.mortgageSubmit.invalidAmount("Property value must be greater than 0");
        }
        if (application.getDownPayment() == null || application.getDownPayment().compareTo(BigDecimal.ZERO) < 0) {
            throw mortgageErrors.mortgageSubmit.invalidAmount("Down payment cannot be negative");
        }
        if (application.getInterestRate() == null || application.getInterestRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw mortgageErrors.mortgageSubmit.invalidAmount("Interest rate must be greater than 0");
        }
    }

    private void validateTerm(MortgageApplication application) {
        if (application.getTermYears() == null || application.getTermYears() <= 0) {
            throw mortgageErrors.mortgageSubmit.invalidAmount("Term years must be greater than 0");
        }
    }

    private void validateIncome(MortgageApplication application) {
        if (application.getAnnualIncome() == null || application.getAnnualIncome().compareTo(BigDecimal.ZERO) <= 0) {
            throw mortgageErrors.mortgageSubmit.invalidAmount("Annual income must be greater than 0");
        }
    }

    private void validateEmploymentStatus(MortgageApplication application) {
        if (application.getEmploymentStatus() == null) {
            throw mortgageErrors.mortgageSubmit.missingField("Employment status is required");
        }
    }
}
