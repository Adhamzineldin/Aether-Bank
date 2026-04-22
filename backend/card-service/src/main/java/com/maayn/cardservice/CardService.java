package com.maayn.cardservice;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.repository.CardRepository;
import com.maayn.cardservice.service.*;
import com.maayn.cardservice.util.DemoPanGenerator;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.errors.*;
import maayn.veld.generated.models.card.*;
import maayn.veld.generated.services.ICardService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService implements ICardService {

    private final CardDetailsQueryService cardDetailsQueryService;
    private final CardPaymentService cardPaymentService;
    private final CardRefundService cardRefundService;
    private final CardVoidService cardVoidService;
    private final CardTransactionHistoryService cardTransactionHistoryService;
    private final CardRepository cardRepository;

    @Override
    public CardDetailsResponse getCardDetails(String cardId) throws GetCardDetailsException, Exception {
        return cardDetailsQueryService.getCardDetails(cardId);
    }

    @Override
    public PanRevealResponse revealPan(String cardId) throws RevealPanException, Exception {
        try {
            UUID uuid = UUID.fromString(cardId);
            Card card = cardRepository.findById(uuid)
                    .orElseThrow(() -> CardErrors.revealPan.notFound("Card not found: " + cardId));

            backfillCredentialsIfNeeded(card);

            return new PanRevealResponse(card.getPan(), card.getCvv());
        } catch (IllegalArgumentException e) {
            throw CardErrors.revealPan.notFound("Invalid card ID: " + cardId);
        }
    }

    /**
     * Legacy rows may have a {@code null} or non-Luhn-valid PAN (the synthetic
     * fallback used to be generated on the fly per request and was not
     * persisted). Materialise a fresh Luhn-valid PAN/CVV the first time we
     * reveal so that:
     * <ul>
     *   <li>the public {@code <PaymentGateway/>} form's mod-10 check passes,</li>
     *   <li>{@link com.maayn.cardservice.service.CardAccessService#getCardByToken}
     *       can resolve the card by PAN on subsequent merchant payments,</li>
     *   <li>repeated reveals return a stable value.</li>
     * </ul>
     * No-op when the stored values are already valid.
     */
    private void backfillCredentialsIfNeeded(Card card) {
        boolean dirty = false;
        if (card.getPan() == null || card.getPan().isBlank() || !DemoPanGenerator.isLuhnValid(card.getPan())) {
            String fresh = DemoPanGenerator.generatePan(card.getCardNetwork());
            card.setPan(fresh);
            card.setLastFourDigits(DemoPanGenerator.lastFourFromPan(fresh));
            dirty = true;
        }
        if (card.getCvv() == null || card.getCvv().isBlank()) {
            card.setCvv(DemoPanGenerator.generateCvv(card.getCardNetwork()));
            dirty = true;
        }
        if (dirty) {
            cardRepository.save(card);
        }
    }

    @Override
    public CardTransactionResponse processMerchantPayment(MerchantPaymentRequest input) throws ProcessMerchantPaymentException, Exception {
        return cardPaymentService.process(input);
    }

    @Override
    public CardTransactionResponse refundTransaction(RefundCardTransactionRequest input) throws RefundTransactionException, Exception {
        return cardRefundService.refund(input);
    }

    @Override
    public CardTransactionResponse voidTransaction(VoidCardTransactionRequest input) throws VoidTransactionException, Exception {
        return cardVoidService.voidTransaction(input);
    }

    @Override
    public PaginatedCardTransactionResponse getCardTransactions(String cardId, GetCardTransactionsRequest input) throws GetCardTransactionsException, Exception {
        return cardTransactionHistoryService.getTransactions(cardId, input);
    }
}
