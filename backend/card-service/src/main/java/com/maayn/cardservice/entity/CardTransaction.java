package com.maayn.cardservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import maayn.veld.generated.models.card.CardTransactionStatus;
import maayn.veld.generated.models.card.CardTransactionType;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
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
}
