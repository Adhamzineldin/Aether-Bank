package com.maayn.cardservice.validator;

import com.maayn.cardservice.entity.Card;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.models.card.CardStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantPaymentValidator {

    private final IbanValidator ibanValidator;
    private final CvvValidator cvvValidator;
    private final ExpiryDateValidator expiryDateValidator;

    public void validate(String iban, String cvv, String expiryDate, Card card) {
        validateCard(card);
        ibanValidator.validate(iban);
        cvvValidator.validate(cvv);
        expiryDateValidator.validate(expiryDate);
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
