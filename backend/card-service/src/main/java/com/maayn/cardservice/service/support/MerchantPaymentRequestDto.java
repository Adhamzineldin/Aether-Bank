package com.maayn.cardservice.service.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Merchant Payment Request DTO (SOLID: Interface Segregation).
 * Clean separation of concerns for API input validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantPaymentRequestDto {

    private String cardToken;
    private String merchantId;
    private String iban;
    private String cvv;
    private String expiryDate;
    private BigDecimal amount;
    private String currency;
    private String idempotencyKey;

    /**
     * Validates required fields.
     */
    public void validate() {
        if (cardToken == null || cardToken.isBlank()) {
            throw new IllegalArgumentException("Card token is required");
        }
        if (merchantId == null || merchantId.isBlank()) {
            throw new IllegalArgumentException("Merchant ID is required");
        }
        if (iban == null || iban.isBlank()) {
            throw new IllegalArgumentException("IBAN is required");
        }
        if (cvv == null || cvv.isBlank()) {
            throw new IllegalArgumentException("CVV is required");
        }
        if (expiryDate == null || expiryDate.isBlank()) {
            throw new IllegalArgumentException("Expiry date is required");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key is required");
        }
    }
}
