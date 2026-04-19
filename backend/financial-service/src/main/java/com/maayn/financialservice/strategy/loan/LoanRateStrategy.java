package com.maayn.financialservice.strategy.loan;

import com.maayn.financialservice.domain.loan.RateChange;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LoanRateStrategy {
    BigDecimal resolveAnnualRate(BigDecimal baseRate, LocalDate date, List<RateChange> history);
}
