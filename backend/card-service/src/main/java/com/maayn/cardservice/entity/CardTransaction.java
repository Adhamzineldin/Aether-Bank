package com.maayn.cardservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import maayn.veld.generated.models.card.CardTransactionStatus;
import maayn.veld.generated.models.card.CardTransactionType;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "card_transactions")
public class CardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Column(name = "merchant_id")
    private UUID merchantId;

    @Column(name = "auth_code", length = 32)
    private String authCode;

    @Column(name = "ledger_reference", length = 64)
    private String ledgerReference;

    @Column(name = "original_transaction_id")
    private UUID originalTransactionId;

    @Column(name = "idempotency_key", unique = true, length = 120)
    private String idempotencyKey;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "amount_in_base_currency", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountInBaseCurrency;

    @Column(name = "exchange_rate", precision = 19, scale = 6)
    private BigDecimal exchangeRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CardTransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CardTransactionType type;

    @CreationTimestamp
    @Column(name = "processed_at", nullable = false, updatable = false)
    private LocalDateTime processedAt;

    @PrePersist
    void prePersist() {
        if (processedAt == null) {
            processedAt = LocalDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(UUID merchantId) {
        this.merchantId = merchantId;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getLedgerReference() {
        return ledgerReference;
    }

    public void setLedgerReference(String ledgerReference) {
        this.ledgerReference = ledgerReference;
    }

    public UUID getOriginalTransactionId() {
        return originalTransactionId;
    }

    public void setOriginalTransactionId(UUID originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getAmountInBaseCurrency() {
        return amountInBaseCurrency;
    }

    public void setAmountInBaseCurrency(BigDecimal amountInBaseCurrency) {
        this.amountInBaseCurrency = amountInBaseCurrency;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public CardTransactionStatus getStatus() {
        return status;
    }

    public void setStatus(CardTransactionStatus status) {
        this.status = status;
    }

    public CardTransactionType getType() {
        return type;
    }

    public void setType(CardTransactionType type) {
        this.type = type;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
