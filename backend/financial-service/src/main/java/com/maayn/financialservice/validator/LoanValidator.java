package com.maayn.financialservice.validator;

import maayn.veld.generated.errors.BadRequestException;
import maayn.veld.generated.models.loan.LoanApplication;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LoanValidator {

    public void validateSubmission(LoanApplication application) {
        validateCustomer(application);
        validateProduct(application);
        validateAmount(application);
        validateTenure(application);
        validateIncome(application);
        validatePurpose(application);
        validateEmploymentStatus(application);
    }

    private void validateCustomer(LoanApplication application) {
        if (application.getCustomerId() == null)
            throw new BadRequestException("Customer ID is required");
    }

    private void validateProduct(LoanApplication application) {
        if (application.getProductId() == null)
            throw new BadRequestException("Loan product ID is required");
    }

    private void validateAmount(LoanApplication application) {
        if (application.getRequestedAmount() == null || application.getRequestedAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new BadRequestException("Requested amount must be greater than 0");
    }

    private void validateTenure(LoanApplication application) {
        if (application.getRequestedTenure() == null || application.getRequestedTenure() <= 0)
            throw new BadRequestException("Requested tenure must be greater than 0");
    }

    private void validateIncome(LoanApplication application) {
        if (application.getAnnualIncome() == null || application.getAnnualIncome().compareTo(BigDecimal.ZERO) <= 0)
            throw new BadRequestException("Annual income must be greater than 0");
    }

    private void validatePurpose(LoanApplication application) {
        if (application.getPurpose() == null || application.getPurpose().isBlank())
            throw new BadRequestException("Loan purpose cannot be empty");
    }

    private void validateEmploymentStatus(LoanApplication application) {
        if (application.getEmploymentStatus() == null)
            throw new BadRequestException("Employment status is required");
    }
}
