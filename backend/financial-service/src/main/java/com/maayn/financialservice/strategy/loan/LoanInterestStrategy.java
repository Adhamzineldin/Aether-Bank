package com.maayn.financialservice.strategy.loan;

import com.maayn.financialservice.domain.loan.InterestDetail;
import com.maayn.financialservice.domain.loan.RateChange;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LoanInterestStrategy {
    InterestDetail calculate(BigDecimal principal, BigDecimal annualRate, LocalDate from, LocalDate to, List<RateChange> rateHistory);
}
