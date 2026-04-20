package com.maayn.cardservice.validator;

import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
public class ExpiryDateValidator {

    public void validate(String expiryDate) {
        if (expiryDate == null || expiryDate.isBlank()) {
            throw new IllegalArgumentException("Expiry date cannot be null or empty");
        }
        if (!expiryDate.matches("^(0[1-9]|1[0-2])/\\d{2}$")) {
            throw new IllegalArgumentException("Expiry date must be in MM/YY format");
        }
        String[] parts = expiryDate.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = 2000 + Integer.parseInt(parts[1]);
        if (YearMonth.of(year, month).isBefore(YearMonth.now())) {
            throw new IllegalArgumentException("Card has expired");
        }
    }
}
