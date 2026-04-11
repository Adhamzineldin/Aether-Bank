package com.maayn.transactionservice.entity;

import maayn.veld.generated.models.transaction.TransactionStatus;


import jakarta.persistence.*;
import maayn.veld.generated.models.transaction.TransactionType;
import org.hibernate.annotations.CreationTimestamp;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "source_account_id")
    private UUID sourceAccountId;

    @Column(name = "destination_account_id")
    private UUID destinationAccountId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(name = "idempotency_key", unique = true, nullable = false)
    private String idempotencyKey;

    @Column(name = "reference_number", unique = true, nullable = false)
    private String referenceNumber;

    @Column(nullable = false)
    private String currency;

    @Column(name = "source_currency")
    private String sourceCurrency;

    @Column(name = "destination_currency")
    private String destinationCurrency;

    @Column(name = "destination_amount")
    private BigDecimal destinationAmount;

    @Column(name = "exchange_rate")
    private BigDecimal exchangeRate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Version
    private Long version;

    @PrePersist
    private void ensureCreatedAt() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void applySagaResult(TransactionStatus finalStatus, String reason) {
        this.status = finalStatus;
        if (finalStatus == TransactionStatus.FAILED) {
            this.failureReason = reason;
        }
    }

}
