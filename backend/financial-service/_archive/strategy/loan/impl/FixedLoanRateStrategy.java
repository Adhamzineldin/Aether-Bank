package com.maayn.financialservice.strategy.loan.impl;

import com.maayn.financialservice.domain.loan.RateChange;
import com.maayn.financialservice.strategy.loan.LoanRateStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component("loanFixedRateStrategy")
public class FixedLoanRateStrategy implements LoanRateStrategy {
    @Override
    public BigDecimal resolveAnnualRate(BigDecimal baseRate, LocalDate date, List<RateChange> history) {
        return baseRate;
    }
}
