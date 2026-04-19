package com.maayn.financialservice.domain.loan;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InterestDetail(
        LocalDate periodStart,
        LocalDate periodEnd,
        long daysCount,
        BigDecimal rateApplied,
        BigDecimal principalBase,
        BigDecimal interestAmount,
        boolean variableRate,
        String notes
) {
}
