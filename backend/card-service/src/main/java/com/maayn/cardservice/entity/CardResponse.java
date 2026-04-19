package com.maayn.cardservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import maayn.veld.generated.models.card.CardStatus;
import maayn.veld.generated.models.card.CardType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Card response DTO for API responses.
 * Contains public-safe card information for external clients.
 * 
 * This DTO should NOT expose sensitive card details (full card number, etc).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {

    private UUID id;
    private UUID accountId;
    private UUID customerId;
    private String cardToken;
    private String lastFourDigits;
    private CardType cardType;
    private CardStatus status;
    private Integer expiryMonth;
    private Integer expiryYear;
    private LocalDateTime issuedAt;
    private LocalDateTime activatedAt;
    private LocalDateTime blockedAt;
}
