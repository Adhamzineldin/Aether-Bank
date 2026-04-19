package com.maayn.cardservice.validator;

import org.springframework.stereotype.Component;

@Component
public class CvvValidator {

    public void validate(String cvv) {
        if (cvv == null || cvv.isBlank()) {
            throw new IllegalArgumentException("CVV cannot be null or empty");
        }
        String clean = cvv.trim();
        if (!clean.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("CVV must contain only digits");
        }
        if (clean.length() != 3 && clean.length() != 4) {
            throw new IllegalArgumentException("CVV must be 3 or 4 digits");
        }
    }
}
