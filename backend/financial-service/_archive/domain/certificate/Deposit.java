package com.maayn.financialservice.domain.certificate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Deposit(
        UUID depositId,
        UUID certificateId,
        BigDecimal amount,
        LocalDateTime depositDate,
        String sourceAccount,
        String status
) {
}
