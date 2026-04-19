package com.maayn.cardservice.Validators;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * CVV Validation (SOLID: Single Responsibility).
 * Validates Card Verification Value format.
 */
@Slf4j
@Component
public class CvvValidator {

    private static final int CVV_LENGTH_AMEX = 4;
    private static final int CVV_LENGTH_STANDARD = 3;

    /**
     * Validates CVV format.
     */
    public void validate(String cvv) {
        if (cvv == null || cvv.isBlank()) {
            throw new IllegalArgumentException("CVV cannot be null or empty");
        }

        String cleanCvv = cvv.trim();

        if (!cleanCvv.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("CVV must contain only digits");
        }

        if (cleanCvv.length() != CVV_LENGTH_STANDARD && cleanCvv.length() != CVV_LENGTH_AMEX) {
            throw new IllegalArgumentException("CVV must be 3 or 4 digits");
        }

        log.debug("CVV validation successful");
    }
}
