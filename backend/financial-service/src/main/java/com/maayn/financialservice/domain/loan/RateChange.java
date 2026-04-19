package com.maayn.financialservice.domain.loan;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RateChange(
        LocalDate effectiveDate,
        BigDecimal annualRate,
        String source,
        String notes
) {
}
