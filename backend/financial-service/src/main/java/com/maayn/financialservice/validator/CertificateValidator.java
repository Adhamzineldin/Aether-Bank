package com.maayn.financialservice.validator;

import com.maayn.financialservice.model.CertificateApplicationRequest;
import maayn.veld.generated.errors.BadRequestException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CertificateValidator {

    public void validateSubmission(CertificateApplicationRequest application) {
        validateCustomer(application);
        validateAccount(application);
        validatePrincipal(application);
        validateInterestRate(application);
        validateTerm(application);
    }

    private void validateCustomer(CertificateApplicationRequest application) {
        if (application.getCustomerId() == null)
            throw new BadRequestException("Customer ID is required");
    }

    private void validateAccount(CertificateApplicationRequest application) {
        if (application.getAccountId() == null)
            throw new BadRequestException("Account ID is required");
    }

    private void validatePrincipal(CertificateApplicationRequest application) {
        if (application.getPrincipal() == null || application.getPrincipal().compareTo(BigDecimal.ZERO) <= 0)
            throw new BadRequestException("Principal must be greater than 0");
    }

    private void validateInterestRate(CertificateApplicationRequest application) {
        if (application.getInterestRate() == null || application.getInterestRate().compareTo(BigDecimal.ZERO) <= 0)
            throw new BadRequestException("Interest rate must be greater than 0");
    }

    private void validateTerm(CertificateApplicationRequest application) {
        if (application.getTermDays() == null || application.getTermDays() <= 0)
            throw new BadRequestException("Term days must be greater than 0");
    }
}
