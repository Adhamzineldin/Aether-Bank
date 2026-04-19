package com.maayn.cardservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import maayn.veld.generated.models.card.CardTransactionStatus;
import maayn.veld.generated.models.card.CardTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Card transaction response DTO for API responses.
 * Contains transaction details for client consumption.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTransactionResponse {

    private UUID id;
    private UUID cardId;
    private String authCode;
    private BigDecimal amount;
    private String currency;
    private CardTransactionType type;
    private CardTransactionStatus status;
    private String ledgerReference;
    private String idempotencyKey;
    private UUID originalTransactionId;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
}
