package com.maayn.cardservice.util;

import maayn.veld.generated.models.card.CardNetwork;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Generates demo card numbers (PCI: not real issuer BINs — sandbox-style only).
 * New cards get a Luhn-valid PAN plus a demo CVV; legacy rows without stored
 * values get stable synthetic PAN/CVV derived from the card id.
 */
public final class DemoPanGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private DemoPanGenerator() {}

    /** 6-digit BIN per network + 9 random digits + Luhn check digit (16 total). */
    public static String generatePan(CardNetwork network) {
        String bin = bin6(network);
        StringBuilder sb = new StringBuilder(bin);
        while (sb.length() < 15) {
            sb.append(RANDOM.nextInt(10));
        }
        sb.append(luhnCheckDigitForFirst15(sb.toString()));
        return sb.toString();
    }

    public static String lastFourFromPan(String panDigits) {
        if (panDigits == null || panDigits.length() < 4) return "0000";
        return panDigits.substring(panDigits.length() - 4);
    }

    /**
     * Deterministic fallback when {@code cards.pan} is null (DB created before
     * this column existed). Not necessarily Luhn-valid.
     */
    public static String syntheticLegacyPan(UUID cardId, CardNetwork network, String lastFour) {
        String bin = bin6(network);
        int mid = (int) (Math.abs(cardId.getMostSignificantBits() ^ cardId.getLeastSignificantBits()) % 1_000_000);
        String mid6 = String.format("%06d", mid);
        String tail = normalizeLastFour(lastFour);
        return bin + mid6 + tail;
    }

    /** AmEx uses 4-digit CID; Visa/Mastercard use 3-digit CVV. */
    public static String generateCvv(CardNetwork network) {
        int len = network == CardNetwork.AMEX ? 4 : 3;
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    public static String syntheticLegacyCvv(UUID cardId, CardNetwork network) {
        int len = network == CardNetwork.AMEX ? 4 : 3;
        int mod = (int) Math.pow(10, len);
        int n = (int) (Math.abs(cardId.getLeastSignificantBits()) % mod);
        return len == 4 ? String.format("%04d", n) : String.format("%03d", n);
    }

    private static String bin6(CardNetwork network) {
        if (network == null) return "400000";
        return switch (network) {
            case VISA -> "453201";
            case MASTERCARD -> "542523";
            case AMEX -> "340000";
        };
    }

    private static String normalizeLastFour(String lastFour) {
        if (lastFour == null || lastFour.isBlank()) return "0000";
        String d = lastFour.replaceAll("\\D", "");
        if (d.length() >= 4) return d.substring(d.length() - 4);
        return String.format("%4s", d).replace(' ', '0');
    }

    /**
     * Finds d ∈ [0..9] so {@code first15 + d} passes the Luhn mod-10 check.
     */
    static char luhnCheckDigitForFirst15(String first15) {
        for (char d = '0'; d <= '9'; d++) {
            if (luhnValid(first15 + d)) return d;
        }
        return '0';
    }

    /**
     * Luhn mod-10: from the right, double every second digit (1-based position
     * from the right: 1 = no double, 2 = double, 3 = no double, …).
     */
    private static boolean luhnValid(String pan16) {
        int sum = 0;
        int n = pan16.length();
        for (int i = n - 1; i >= 0; i--) {
            int digit = pan16.charAt(i) - '0';
            int posFromRight = n - i; // 1 = rightmost
            if (posFromRight % 2 == 0) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
        }
        return sum % 10 == 0;
    }
}
