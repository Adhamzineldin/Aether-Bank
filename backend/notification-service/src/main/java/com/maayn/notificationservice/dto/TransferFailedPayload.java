package com.maayn.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

//  JSON shape published by the transaction service for transaction.transfer.failed

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransferFailedPayload(
        String referenceNumber,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currency,
        LocalDateTime eventTime,
        String failureReason
) {
}
