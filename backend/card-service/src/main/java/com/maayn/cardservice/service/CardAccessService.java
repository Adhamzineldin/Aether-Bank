package com.maayn.cardservice.service;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CardTransaction;
import com.maayn.cardservice.repository.CardRepository;
import com.maayn.cardservice.repository.CardTransactionRepository;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.errors.*;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardAccessService {

    private final CardRepository cardRepository;
    private final CardTransactionRepository cardTransactionRepository;

    public Card getCardDetailsCard(String cardId) throws GetCardDetailsException {
        return cardRepository.findById(parseUuid(cardId, CardErrors.getCardDetails.notFound("Card not found")))
                .orElseThrow(() -> CardErrors.getCardDetails.notFound("Card not found"));
    }

    public UUID getExistingCardId(String cardId) throws GetCardTransactionsException {
        UUID parsedId = parseUuid(cardId, CardErrors.getCardTransactions.notFound("Card not found"));
        if (!cardRepository.existsById(parsedId)) {
            throw CardErrors.getCardTransactions.notFound("Card not found");
        }
        return parsedId;
    }

    /**
     * Resolves the {@link Card} a merchant payment is targeting.
     *
     * <p>Real PSPs require a server-issued tokenized handle, but for the demo
     * checkout flow the public {@code <PaymentGateway/>} form lets the user
     * type a raw card number. We therefore accept either:</p>
     * <ul>
     *   <li>the stored {@code card_token} (e.g. {@code card_abc123...}), or</li>
     *   <li>a raw PAN — digits and spaces are tolerated and stripped.</li>
     * </ul>
     *
     * <p>The token lookup is tried first so production-shaped payloads keep
     * their fast path; the PAN fallback only runs when the literal value
     * doesn't match any token.</p>
     */
    public Card getCardByToken(String cardToken) throws ProcessMerchantPaymentException {
        if (cardToken == null || cardToken.isBlank()) {
            throw CardErrors.processMerchantPayment.invalidCardToken("Card token is invalid");
        }
        Optional<Card> byToken = cardRepository.findByCardToken(cardToken);
        if (byToken.isPresent()) {
            return byToken.get();
        }
        // Fallback: treat the value as a raw PAN (strip spaces / dashes).
        String normalisedPan = cardToken.replaceAll("[\\s-]", "");
        if (!normalisedPan.isEmpty() && normalisedPan.chars().allMatch(Character::isDigit)) {
            Optional<Card> byPan = cardRepository.findByPan(normalisedPan);
            if (byPan.isPresent()) {
                return byPan.get();
            }
        }
        throw CardErrors.processMerchantPayment.invalidCardToken("Card token is invalid");
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

    private <E extends RuntimeException> UUID parseUuid(String raw, E fallbackException) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            throw fallbackException;
        }
    }
}
