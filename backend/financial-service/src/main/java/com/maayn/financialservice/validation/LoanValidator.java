package com.maayn.financialservice.validation;

import maayn.veld.generated.errors.loanErrors;
import maayn.veld.generated.models.loan.LoanApplication;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LoanValidator {

    public void validateSubmission(LoanApplication application) {
        validateCustomer(application);
        validateAccount(application);
        validateProduct(application);
        validateLoanType(application);
        validatePositiveAmount(application);
        validateTenure(application);
        validateIncome(application);
        validatePurpose(application);
        validateEmploymentStatus(application);
    }

    private void validateCustomer(LoanApplication application) {
        if (application.getCustomerId() == null) {
            throw loanErrors.LoanSubmitErrors.missingField(
                    "Customer ID is required"
            );
        }
    }

    private void validateAccount(LoanApplication application) {
        if (application.getAccountId() == null) {
            throw loanErrors.LoanSubmitErrors.missingField(
                    "Disbursement account ID is required"
            );
        }
    }

    private void validateProduct(LoanApplication application) {
        if (application.getProductId() == null) {
            throw loanErrors.LoanSubmitErrors.missingField(
                    "Loan product ID is required"
            );
        }
    }

    private void validateLoanType(LoanApplication application) {
        if (application.getLoanType() == null) {
            throw loanErrors.LoanSubmitErrors.missingField(
                    "Loan type is required"
            );
        }
    }

    private void validatePositiveAmount(LoanApplication application) {
        if (application.getRequestedAmount() == null ||
                application.getRequestedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw loanErrors.LoanSubmitErrors.invalidAmount(
                    "Requested amount must be greater than 0"
            );
        }
    }

    private void validateTenure(LoanApplication application) {
        if (application.getRequestedTenure() == null ||
                application.getRequestedTenure() <= 0) {
            throw loanErrors.LoanSubmitErrors.invalidAmount(
                    "Requested tenure must be greater than 0"
            );
        }
    }

    private void validateIncome(LoanApplication application) {
        if (application.getAnnualIncome() == null ||
                application.getAnnualIncome().compareTo(BigDecimal.ZERO) <= 0) {
            throw loanErrors.LoanSubmitErrors.invalidAmount(
                    "Annual income must be greater than 0"
            );
        }
    }

    private void validatePurpose(LoanApplication application) {
        if (application.getPurpose() == null ||
                application.getPurpose().trim().isEmpty()) {
            throw loanErrors.LoanSubmitErrors.missingField(
                    "Loan purpose cannot be empty"
            );
        }
    }

    private void validateEmploymentStatus(LoanApplication application) {
        if (application.getEmploymentStatus() == null) {
            throw loanErrors.LoanSubmitErrors.missingField(
                    "Employment status is required"
            );
        }
    }
}
