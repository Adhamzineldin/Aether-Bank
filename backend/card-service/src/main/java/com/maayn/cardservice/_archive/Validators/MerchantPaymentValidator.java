package com.maayn.cardservice.Validators;
import com.maayn.cardservice.entity.Card;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.card.CardStatus;
import org.springframework.stereotype.Component;

/**
 * Merchant Payment Validator (SOLID: Single Responsibility).
 * Validates all merchant payment requirements (IBAN, CVV, expiry, card state).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantPaymentValidator {

    private final IbanValidator ibanValidator;
    private final CvvValidator cvvValidator;
    private final ExpiryDateValidator expiryDateValidator;

    /**
     * Comprehensive validation for merchant payments.
     */
    public void validateMerchantPayment(String iban, String cvv, String expiryDate, Card card) {
        validateCard(card);
        ibanValidator.validate(iban);
        cvvValidator.validate(cvv);
        expiryDateValidator.validate(expiryDate);
        
        log.info("Merchant payment validation successful for card: {}", card.getId());
    }

    private void validateCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalArgumentException("Card is not active. Current status: " + card.getStatus());
        }

        if (card.getBlockedAt() != null) {
            throw new IllegalArgumentException("Card is blocked: " + card.getBlockReason());
        }
    }
}
