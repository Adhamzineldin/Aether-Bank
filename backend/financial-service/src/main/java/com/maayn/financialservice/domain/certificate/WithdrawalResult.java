package com.maayn.financialservice.domain.certificate;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawalResult(
        UUID certificateId,
        BigDecimal requestedAmount,
        BigDecimal penaltyAmount,
        BigDecimal netAmount,
        String status,
        String notes
) {
}
