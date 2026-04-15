package com.maayn.financialservice.validation;

import com.maayn.financialservice.entity.LoanApplication;
import maayn.veld.generated.errors.LoanErrors;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LoanValidator {

    public void validateSubmission(LoanApplication application) {
        validateCustomer(application);
        validateProduct(application);
        validatePositiveAmount(application);
        validateTenure(application);
        validateIncome(application);
        validatePurpose(application);
        validateEmploymentStatus(application);
    }

    private void validateCustomer(LoanApplication application) {
        if (application.getCustomerId() == null) {
            throw LoanErrors.LoanApplicationErrors.invalidCustomer(
                    "Customer ID is required"
            );
        }
    }

    private void validateProduct(LoanApplication application) {
        if (application.getProductId() == null) {
            throw LoanErrors.LoanApplicationErrors.invalidProduct(
                    "Loan product ID is required"
            );
        }
    }

    private void validatePositiveAmount(LoanApplication application) {
        if (application.getRequestedAmount() == null ||
                application.getRequestedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw LoanErrors.LoanApplicationErrors.invalidAmount(
                    "Requested amount must be greater than 0"
            );
        }
    }

    private void validateTenure(LoanApplication application) {
        if (application.getRequestedTenureMonths() == null ||
                application.getRequestedTenureMonths() <= 0) {
            throw LoanErrors.LoanApplicationErrors.invalidTenure(
                    "Requested tenure must be greater than 0"
            );
        }
    }

    private void validateIncome(LoanApplication application) {
        if (application.getAnnualIncome() == null ||
                application.getAnnualIncome().compareTo(BigDecimal.ZERO) <= 0) {
            throw LoanErrors.LoanApplicationErrors.invalidIncome(
                    "Annual income must be greater than 0"
            );
        }
    }

    private void validatePurpose(LoanApplication application) {
        if (application.getPurpose() == null ||
                application.getPurpose().trim().isEmpty()) {
            throw LoanErrors.LoanApplicationErrors.invalidPurpose(
                    "Loan purpose cannot be empty"
            );
        }
    }

    private void validateEmploymentStatus(LoanApplication application) {
        if (application.getEmploymentStatus() == null) {
            throw LoanErrors.LoanApplicationErrors.invalidEmploymentStatus(
                    "Employment status is required"
            );
        }
    }
}