package com.maayn.cardservice.validator;

import org.springframework.stereotype.Component;

@Component
public class IbanValidator {

    private static final int IBAN_MIN_LENGTH = 15;
    private static final int IBAN_MAX_LENGTH = 34;

    public void validate(String iban) {
        if (iban == null || iban.isBlank()) {
            throw new IllegalArgumentException("IBAN cannot be null or empty");
        }
        String clean = iban.replaceAll("\\s+", "").toUpperCase();
        if (clean.length() < IBAN_MIN_LENGTH || clean.length() > IBAN_MAX_LENGTH) {
            throw new IllegalArgumentException("IBAN length must be between 15 and 34 characters");
        }
        if (!clean.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]+$")) {
            throw new IllegalArgumentException("IBAN format is invalid");
        }
        if (!validateChecksum(clean)) {
            throw new IllegalArgumentException("IBAN checksum validation failed");
        }
    }

    private boolean validateChecksum(String iban) {
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            numeric.append(Character.isDigit(c) ? c : Character.getNumericValue(c));
        }
        long mod = 0;
        for (char digit : numeric.toString().toCharArray()) {
            mod = (mod * 10 + Character.getNumericValue(digit)) % 97;
        }
        return mod == 1;
    }
}
