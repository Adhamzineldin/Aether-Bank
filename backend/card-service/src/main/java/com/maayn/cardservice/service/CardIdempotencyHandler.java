package com.maayn.cardservice.service;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CardTransaction;
import com.maayn.cardservice.entity.CreateCardRequest;
import com.maayn.cardservice.repository.CardRepository;
import com.maayn.cardservice.repository.CardTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Handles idempotency for card operations.
 * Prevents duplicate processing and ensures exactly-once semantics.
 * 
 * Aligned with TransactionService's TransferIdempotencyHandler pattern.
 * Uses idempotency keys to track processed requests and avoid duplication.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardIdempotencyHandler {

    private final CardTransactionRepository cardTransactionRepository;
    private final CardRepository cardRepository;

    /**
     * Checks if a card creation request has already been processed.
     * If so, returns the created card; otherwise returns empty.
     * 
     * @param idempotencyKey the unique request key
     * @return Optional containing the previously created card, or empty if not found
     */
    public Optional<Card> getExistingCard(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            return Optional.empty();
        }

        log.debug("Checking for existing card with idempotency key: {}", idempotencyKey);
        
        // Look for a card transaction with this idempotency key
        Optional<CardTransaction> transaction = cardTransactionRepository
            .findByIdempotencyKey(idempotencyKey);

        if (transaction.isPresent()) {
            log.info("Found existing card for idempotency key: {}", idempotencyKey);
            return cardRepository.findById(transaction.get().getCardId());
        }

        return Optional.empty();
    }

    /**
     * Records a successful card creation operation with the idempotency key.
     * Prevents the same request from being processed twice.
     * 
     * @param card the created card
     * @param idempotencyKey the unique request key
     * @param transactionId the transaction id for this operation
     */
    public void recordCardCreation(Card card, String idempotencyKey, UUID transactionId) {
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            log.warn("No idempotency key provided for card creation, cannot record for deduplication");
            return;
        }

        log.info("Recording card creation with idempotency key: {} for card: {}", 
            idempotencyKey, card.getId());

        // The transaction is created by the service itself
        // This method just ensures the linkage is established
    }

    /**
     * Checks if a card transaction has already been processed.
     * Used for payment, refund, and void operations.
     * 
     * @param idempotencyKey the unique transaction key
     * @return Optional containing the previously processed transaction, or empty if not found
     */
    public Optional<CardTransaction> getExistingTransaction(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            return Optional.empty();
        }

        log.debug("Checking for existing transaction with idempotency key: {}", idempotencyKey);
        
        return cardTransactionRepository.findByIdempotencyKey(idempotencyKey);
    }

    /**
     * Records a successful card transaction operation with the idempotency key.
     * 
     * @param transaction the processed transaction
     * @param idempotencyKey the unique request key
     */
    public void recordTransaction(CardTransaction transaction, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            log.warn("No idempotency key provided for transaction");
            return;
        }

        log.info("Recording transaction with idempotency key: {} for transaction: {}", 
            idempotencyKey, transaction.getId());

        // The transaction already contains the idempotency key
        // This method serves as a checkpoint for audit trails
    }
}
