package com.maayn.financialservice.domain.loan;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RepaymentScheduleLine(
        LocalDate dueDate,
        BigDecimal openingBalance,
        BigDecimal expectedPrincipal,
        BigDecimal expectedInterest,
        BigDecimal expectedFee,
        BigDecimal expectedTotal,
        BigDecimal closingBalance,
        String status
) {
}
