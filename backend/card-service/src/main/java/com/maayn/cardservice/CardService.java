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
            
            String pan = card.getPan() != null
                    ? card.getPan()
                    : DemoPanGenerator.syntheticLegacyPan(card.getId(), card.getCardNetwork(), card.getLastFourDigits());
            
            String cvv = card.getCvv() != null && !card.getCvv().isBlank()
                    ? card.getCvv()
                    : DemoPanGenerator.syntheticLegacyCvv(card.getId(), card.getCardNetwork());
            
            return new PanRevealResponse(pan, cvv);
        } catch (IllegalArgumentException e) {
            throw CardErrors.revealPan.notFound("Invalid card ID: " + cardId);
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
