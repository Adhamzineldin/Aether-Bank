package com.maayn.accountservice.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Year;

@Component
public class AccountNumberGenerator {

    private static final SecureRandom random = new SecureRandom();
    private static final String BANK_CODE = "AETH"; // Aether Bank code

    /**
     * Generates a unique account number in format: AETH-YYYY-XXXXXXXXXX
     * Example: AETH-2026-1234567890
     */
    public String generate() {
        int year = Year.now().getValue();
        long accountSequence = 1000000000L + (long) (random.nextDouble() * 9000000000L);
        return String.format("%s-%d-%d", BANK_CODE, year, accountSequence);
    }
}

