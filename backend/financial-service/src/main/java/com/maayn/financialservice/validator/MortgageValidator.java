package com.maayn.financialservice.validator;

import com.maayn.financialservice.model.MortgageApplicationRequest;
import maayn.veld.generated.errors.BadRequestException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MortgageValidator {

    public void validateSubmission(MortgageApplicationRequest application) {
        validateCustomer(application);
        validatePropertyAddress(application);
        validateAmounts(application);
        validateTerm(application);
        validateIncome(application);
        validateEmploymentStatus(application);
    }

    private void validateCustomer(MortgageApplicationRequest application) {
        if (application.getCustomerId() == null)
            throw new BadRequestException("Customer ID is required");
    }

    private void validatePropertyAddress(MortgageApplicationRequest application) {
        if (application.getPropertyAddress() == null || application.getPropertyAddress().isBlank())
            throw new BadRequestException("Property address is required");
    }

    private void validateAmounts(MortgageApplicationRequest application) {
        if (application.getRequestedAmount() == null || application.getRequestedAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new BadRequestException("Requested amount must be greater than 0");
        if (application.getPropertyValue() == null || application.getPropertyValue().compareTo(BigDecimal.ZERO) <= 0)
            throw new BadRequestException("Property value must be greater than 0");
        if (application.getDownPayment() == null || application.getDownPayment().compareTo(BigDecimal.ZERO) < 0)
            throw new BadRequestException("Down payment cannot be negative");
        if (application.getInterestRate() == null || application.getInterestRate().compareTo(BigDecimal.ZERO) <= 0)
            throw new BadRequestException("Interest rate must be greater than 0");
    }

    private void validateTerm(MortgageApplicationRequest application) {
        if (application.getTermYears() == null || application.getTermYears() <= 0)
            throw new BadRequestException("Term years must be greater than 0");
    }

    private void validateIncome(MortgageApplicationRequest application) {
        if (application.getAnnualIncome() == null || application.getAnnualIncome().compareTo(BigDecimal.ZERO) <= 0)
            throw new BadRequestException("Annual income must be greater than 0");
    }

    private void validateEmploymentStatus(MortgageApplicationRequest application) {
        if (application.getEmploymentStatus() == null)
            throw new BadRequestException("Employment status is required");
    }
}
