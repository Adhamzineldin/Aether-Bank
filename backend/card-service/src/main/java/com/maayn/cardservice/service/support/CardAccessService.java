package com.maayn.cardservice.service.support;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CardTransaction;
import com.maayn.cardservice.repository.CardRepository;
import com.maayn.cardservice.repository.CardTransactionRepository;
import maayn.veld.generated.errors.CardErrors;
import maayn.veld.generated.errors.GetCardDetailsException;
import maayn.veld.generated.errors.GetCardTransactionsException;
import maayn.veld.generated.errors.ProcessMerchantPaymentException;
import maayn.veld.generated.errors.RefundTransactionException;
import maayn.veld.generated.errors.VoidTransactionException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
/**
 * Centralizes repository lookups plus the API-specific errors that should be thrown when data is missing or malformed.
 * This keeps the use-case services focused on business flow rather than lookup boilerplate.
 */
public class CardAccessService {

    private final CardRepository cardRepository;
    private final CardTransactionRepository cardTransactionRepository;

    public CardAccessService(CardRepository cardRepository, CardTransactionRepository cardTransactionRepository) {
        this.cardRepository = cardRepository;
        this.cardTransactionRepository = cardTransactionRepository;
    }

    public Card getCardDetailsCard(String cardId) throws GetCardDetailsException {
        return cardRepository.findById(parseUuid(cardId, CardErrors.getCardDetails.notFound("Card not found")))
                .orElseThrow(() -> CardErrors.getCardDetails.notFound("Card not found"));
    }

    public UUID getExistingCardId(String cardId) throws GetCardTransactionsException {
        UUID parsedCardId = parseUuid(cardId, CardErrors.getCardTransactions.notFound("Card not found"));
        if (!cardRepository.existsById(parsedCardId)) {
            throw CardErrors.getCardTransactions.notFound("Card not found");
        }
        return parsedCardId;
    }

    public Card getCardByToken(String cardToken) throws ProcessMerchantPaymentException {
        return cardRepository.findByCardToken(cardToken)
                .orElseThrow(() -> CardErrors.processMerchantPayment.invalidCardToken("Card token is invalid"));
    }

    public CardTransaction getRefundableTransaction(UUID transactionId) throws RefundTransactionException {
        return cardTransactionRepository.findById(transactionId)
                .orElseThrow(() -> CardErrors.refundTransaction.transactionNotFound("Card transaction not found"));
    }

    public CardTransaction getVoidableTransaction(UUID transactionId) throws VoidTransactionException {
        return cardTransactionRepository.findById(transactionId)
                .orElseThrow(() -> CardErrors.voidTransaction.transactionNotFound("Card transaction not found"));
    }

    public Optional<CardTransaction> findTransactionByIdempotencyKey(String idempotencyKey) {
        return cardTransactionRepository.findByIdempotencyKey(idempotencyKey);
    }

    private UUID parseUuid(String raw, RuntimeException exception) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            // Invalid UUIDs are surfaced as the same domain error type the API already expects.
            throw exception;
        }
    }
}
