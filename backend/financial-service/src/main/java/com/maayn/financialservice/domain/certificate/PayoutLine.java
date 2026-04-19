package com.maayn.financialservice.domain.certificate;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PayoutLine(
        LocalDate dueDate,
        BigDecimal principalReturn,
        BigDecimal interestAmount,
        BigDecimal totalAmount,
        String status
) {
}
