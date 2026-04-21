package com.maayn.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
@JsonIgnoreProperties(ignoreUnknown = true)
public record TransferSuccessPayload(
        String referenceNumber,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currency,
        LocalDateTime eventTime
) {
}
