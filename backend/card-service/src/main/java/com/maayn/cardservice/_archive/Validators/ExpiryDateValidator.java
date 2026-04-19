package com.maayn.cardservice.Validators;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Expiry Date Validation (SOLID: Single Responsibility).
 * Validates card expiry dates.
 */
@Slf4j
@Component
public class ExpiryDateValidator {

    /**
     * Validates expiry date format (MM/YY) and ensures card is not expired.
     *
     * @param expiryDate in MM/YY format
     */
    public void validate(String expiryDate) {
        if (expiryDate == null || expiryDate.isBlank()) {
            throw new IllegalArgumentException("Expiry date cannot be null or empty");
        }

        if (!expiryDate.matches("^(0[1-9]|1[0-2])/\\d{2}$")) {
            throw new IllegalArgumentException("Expiry date must be in MM/YY format");
        }

        try {
            String[] parts = expiryDate.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]);

            // Convert YY to YYYY (assuming 00-99 range)
            int fullYear = 2000 + year;

            YearMonth expiryMonth = YearMonth.of(fullYear, month);
            YearMonth now = YearMonth.now();

            if (expiryMonth.isBefore(now)) {
                throw new IllegalArgumentException("Card has expired");
            }

            log.debug("Expiry date validation successful");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid expiry date format", e);
        }
    }
}
