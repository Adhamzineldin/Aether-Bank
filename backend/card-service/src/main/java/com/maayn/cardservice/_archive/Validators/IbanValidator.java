package com.maayn.cardservice.Validators;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * IBAN Validation (SOLID: Single Responsibility).
 * Validates International Bank Account Numbers.
 */
@Slf4j
@Component
public class IbanValidator {

    private static final int IBAN_MIN_LENGTH = 15;
    private static final int IBAN_MAX_LENGTH = 34;

    /**
     * Validates IBAN format and checksum.
     */
    public void validate(String iban) {
        if (iban == null || iban.isBlank()) {
            throw new IllegalArgumentException("IBAN cannot be null or empty");
        }

        String cleanIban = iban.replaceAll("\\s+", "").toUpperCase();

        if (cleanIban.length() < IBAN_MIN_LENGTH || cleanIban.length() > IBAN_MAX_LENGTH) {
            throw new IllegalArgumentException("IBAN length must be between 15 and 34 characters");
        }

        if (!cleanIban.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]+$")) {
            throw new IllegalArgumentException("IBAN format is invalid");
        }

        if (!validateIbanChecksum(cleanIban)) {
            throw new IllegalArgumentException("IBAN checksum validation failed");
        }

        log.debug("IBAN validation successful: {}", maskIban(iban));
    }

    private boolean validateIbanChecksum(String iban) {
        // Move first 4 characters to the end
        String rearranged = iban.substring(4) + iban.substring(0, 4);

        // Replace letters with numbers (A=10, B=11, ..., Z=35)
        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isDigit(c)) {
                numeric.append(c);
            } else {
                numeric.append(Character.getNumericValue(c));
            }
        }

        // Calculate mod 97
        long mod = 0;
        for (char digit : numeric.toString().toCharArray()) {
            mod = (mod * 10 + Character.getNumericValue(digit)) % 97;
        }

        return mod == 1;
    }

    private String maskIban(String iban) {
        if (iban == null || iban.length() < 8) return iban;
        return iban.substring(0, 4) + "****" + iban.substring(iban.length() - 4);
    }
}
