package com.maayn.financialservice.domain.certificate;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InterestAccrual(
        LocalDate periodStart,
        LocalDate periodEnd,
        long daysCount,
        BigDecimal rateApplied,
        BigDecimal principalBase,
        BigDecimal interestAmount,
        boolean capitalized,
        String notes
) {
}
