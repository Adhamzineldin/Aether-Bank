package com.maayn.transactionservice.dto;

import maayn.veld.generated.models.transaction.TransactionStatus;
import maayn.veld.generated.models.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A single account statement row, shaped from the perspective of the account
 * being queried. {@code amount}/{@code currency} are always in the viewer's
 * own currency — for FX transfers this is the correctly converted leg, not
 * the raw {@code Transaction.amount} (which is always the source leg). The
 * counterpart leg is surfaced on the side so the UI can show FX context.
 */
public record AccountStatementRow(
        String referenceNumber,
        LocalDateTime timestamp,
        TransactionStatus status,
        TransactionType type,
        Direction direction,
        BigDecimal amount,
        String currency,
        UUID counterpartyAccountId,
        BigDecimal counterpartyAmount,
        String counterpartyCurrency,
        BigDecimal exchangeRate
) {
    public enum Direction { CREDIT, DEBIT }
}
