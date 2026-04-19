package com.maayn.cardservice.service;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CardTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Publishes card-related events to RabbitMQ for event-driven architecture.
 * Aligned with TransactionService's TransactionEventPublisher pattern.
 * 
 * Supports events:
 * - Card issuance/creation
 * - Card activation
 * - Card blocking/closure
 * - Transaction authorization
 * - Transaction posting
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    // Exchange and routing key constants
    private static final String CARD_EXCHANGE = "card.events";
    private static final String CARD_CREATED_ROUTING_KEY = "card.created";
    private static final String CARD_ACTIVATED_ROUTING_KEY = "card.activated";
    private static final String CARD_BLOCKED_ROUTING_KEY = "card.blocked";
    private static final String TRANSACTION_AUTHORIZED_ROUTING_KEY = "card.transaction.authorized";
    private static final String TRANSACTION_POSTED_ROUTING_KEY = "card.transaction.posted";

    /**
     * Publishes a card created event.
     * Called after successful card issuance.
     * 
     * @param card the newly created card
     */
    public void publishCardCreated(Card card) {
        log.info("Publishing card created event for card: {}", card.getId());
        
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "CARD_CREATED");
        event.put("cardId", card.getId());
        event.put("accountId", card.getAccountId());
        event.put("customerId", card.getCustomerId());
        event.put("cardType", card.getCardType());
        event.put("status", card.getStatus());
        event.put("timestamp", LocalDateTime.now());

        try {
            rabbitTemplate.convertAndSend(CARD_EXCHANGE, CARD_CREATED_ROUTING_KEY, event);
            log.debug("Card created event published successfully");
        } catch (Exception e) {
            log.error("Failed to publish card created event", e);
            // Do not throw - event publishing should not block card creation
        }
    }

    /**
     * Publishes a card activated event.
     * Called when a card transitions to ACTIVE status.
     * 
     * @param card the activated card
     */
    public void publishCardActivated(Card card) {
        log.info("Publishing card activated event for card: {}", card.getId());
        
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "CARD_ACTIVATED");
        event.put("cardId", card.getId());
        event.put("accountId", card.getAccountId());
        event.put("status", card.getStatus());
        event.put("activatedAt", card.getActivatedAt());
        event.put("timestamp", LocalDateTime.now());

        try {
            rabbitTemplate.convertAndSend(CARD_EXCHANGE, CARD_ACTIVATED_ROUTING_KEY, event);
            log.debug("Card activated event published successfully");
        } catch (Exception e) {
            log.error("Failed to publish card activated event", e);
        }
    }

    /**
     * Publishes a card blocked event.
     * Called when a card is blocked or closed.
     * 
     * @param card the blocked card
     * @param reason the reason for blocking
     */
    public void publishCardBlocked(Card card, String reason) {
        log.info("Publishing card blocked event for card: {}, reason: {}", card.getId(), reason);
        
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "CARD_BLOCKED");
        event.put("cardId", card.getId());
        event.put("accountId", card.getAccountId());
        event.put("status", card.getStatus());
        event.put("reason", reason);
        event.put("blockedAt", card.getBlockedAt());
        event.put("timestamp", LocalDateTime.now());

        try {
            rabbitTemplate.convertAndSend(CARD_EXCHANGE, CARD_BLOCKED_ROUTING_KEY, event);
            log.debug("Card blocked event published successfully");
        } catch (Exception e) {
            log.error("Failed to publish card blocked event", e);
        }
    }

    /**
     * Publishes a transaction authorization event.
     * Called when a transaction is authorized but not yet posted.
     * 
     * @param transaction the authorized transaction
     * @param card the card used for the transaction
     */
    public void publishTransactionAuthorized(CardTransaction transaction, Card card) {
        log.info("Publishing transaction authorized event for transaction: {}", transaction.getId());
        
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "TRANSACTION_AUTHORIZED");
        event.put("transactionId", transaction.getId());
        event.put("cardId", transaction.getCardId());
        event.put("accountId", card.getAccountId());
        event.put("amount", transaction.getAmount());
        event.put("currency", transaction.getCurrency());
        event.put("type", transaction.getType());
        event.put("authCode", transaction.getAuthCode());
        event.put("status", transaction.getStatus());
        event.put("timestamp", LocalDateTime.now());

        try {
            rabbitTemplate.convertAndSend(CARD_EXCHANGE, TRANSACTION_AUTHORIZED_ROUTING_KEY, event);
            log.debug("Transaction authorized event published successfully");
        } catch (Exception e) {
            log.error("Failed to publish transaction authorized event", e);
        }
    }

    /**
     * Publishes a transaction posted event.
     * Called when a transaction is finalized and funds are settled.
     * 
     * @param transaction the posted transaction
     * @param card the card used for the transaction
     */
    public void publishTransactionPosted(CardTransaction transaction, Card card) {
        log.info("Publishing transaction posted event for transaction: {}", transaction.getId());
        
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "TRANSACTION_POSTED");
        event.put("transactionId", transaction.getId());
        event.put("cardId", transaction.getCardId());
        event.put("accountId", card.getAccountId());
        event.put("amount", transaction.getAmount());
        event.put("currency", transaction.getCurrency());
        event.put("type", transaction.getType());
        event.put("authCode", transaction.getAuthCode());
        event.put("status", transaction.getStatus());
        event.put("processedAt", transaction.getProcessedAt());
        event.put("timestamp", LocalDateTime.now());

        try {
            rabbitTemplate.convertAndSend(CARD_EXCHANGE, TRANSACTION_POSTED_ROUTING_KEY, event);
            log.debug("Transaction posted event published successfully");
        } catch (Exception e) {
            log.error("Failed to publish transaction posted event", e);
        }
    }
}
