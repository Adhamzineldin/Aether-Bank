package com.maayn.cardservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import maayn.veld.generated.models.card.CardNetwork;
import maayn.veld.generated.models.card.CardType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request model for card issuance.
 * Supports creation of DEBIT, CREDIT, and PREPAID card types.
 * 
 * This class separates API input from entity models, allowing validation
 * and transformation logic to occur before database persistence.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCardRequest {

    /**
     * Account identifier - the account this card belongs to.
     * Required for all card types.
     */
    private UUID accountId;

    /**
     * Customer identifier - the cardholder.
     * Required for all card types.
     */
    private UUID customerId;

    /**
     * Card type: DEBIT, CREDIT, or PREPAID.
     * Determines which card creator strategy to use.
     * Required.
     */
    private CardType cardType;

    /**
     * Card network: VISA, MASTERCARD, AMEX, DISCOVER.
     * Determines the card issuer and network rules.
     * Required.
     */
    private CardNetwork cardNetwork;

    /**
     * For CREDIT cards: the credit limit.
     * For DEBIT/PREPAID: the initial balance.
     * Required, must be positive.
     */
    private BigDecimal initialBalance;

    /**
     * For CREDIT cards: annual interest rate (e.g., 0.1899 for 18.99%).
     * Used only when cardType is CREDIT.
     * Optional, defaults to 0 if not provided.
     */
    private BigDecimal annualInterestRate;

    /**
     * For CREDIT cards: day of month for billing cycle.
     * Valid range: 1-28 (to avoid month-end issues).
     * Used only when cardType is CREDIT.
     * Optional, defaults to 1.
     */
    private Integer billingCycleDay;
}
