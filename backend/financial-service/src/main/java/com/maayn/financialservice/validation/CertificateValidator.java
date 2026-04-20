package com.maayn.financialservice.validation;

import maayn.veld.generated.errors.certificateErrors;
import maayn.veld.generated.models.certificate.CertificateApplication;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CertificateValidator {

    public void validateSubmission(CertificateApplication application) {
        validateCustomer(application);
        validateAccount(application);
        validatePrincipal(application);
        validateTerm(application);
        validateInterestRate(application);
    }

    private void validateCustomer(CertificateApplication application) {
        if (application.getCustomerId() == null) {
            throw certificateErrors.certificateSubmit.missingField("Customer ID is required");
        }
    }

    private void validateAccount(CertificateApplication application) {
        if (application.getAccountId() == null) {
            throw certificateErrors.certificateSubmit.missingField("Account ID is required");
        }
    }

    private void validatePrincipal(CertificateApplication application) {
        if (application.getPrincipal() == null || application.getPrincipal().compareTo(BigDecimal.ZERO) <= 0) {
            throw certificateErrors.certificateSubmit.invalidAmount("Principal must be greater than 0");
        }
    }

    private void validateInterestRate(CertificateApplication application) {
        if (application.getInterestRate() == null || application.getInterestRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw certificateErrors.certificateSubmit.invalidAmount("Interest rate must be greater than 0");
        }
    }

    private void validateTerm(CertificateApplication application) {
        if (application.getTermDays() == null || application.getTermDays() <= 0) {
            throw certificateErrors.certificateSubmit.invalidAmount("Term days must be greater than 0");
        }
    }
}
