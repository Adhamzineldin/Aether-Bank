package com.maayn.financialservice.domain.loan;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Payment(
        UUID paymentId,
        UUID loanId,
        BigDecimal amount,
        BigDecimal principalPortion,
        BigDecimal interestPortion,
        BigDecimal feePortion,
        BigDecimal penaltyPortion,
        LocalDateTime paymentDate,
        String reference,
        String status
) {
}
