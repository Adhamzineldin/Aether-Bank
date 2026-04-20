package com.maayn.financialservice.strategy.loan.impl;

import com.maayn.financialservice.domain.common.FinancialMath;
import com.maayn.financialservice.domain.loan.InterestDetail;
import com.maayn.financialservice.domain.loan.RateChange;
import com.maayn.financialservice.strategy.loan.LoanInterestStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component("loanAmortizedInterestStrategy")
public class AmortizedLoanInterestStrategy implements LoanInterestStrategy {
    @Override
    public InterestDetail calculate(BigDecimal principal, BigDecimal annualRate, LocalDate from, LocalDate to, List<RateChange> rateHistory) {
        long days = ChronoUnit.DAYS.between(from, to);
        BigDecimal rate = annualRate == null ? BigDecimal.ZERO : annualRate;
        BigDecimal interest = FinancialMath.money(principal
                .multiply(rate)
                .multiply(BigDecimal.valueOf(days))
                .divide(BigDecimal.valueOf(365), FinancialMath.MC));
        return new InterestDetail(from, to, days, rate, FinancialMath.money(principal), interest, false, "Amortized period interest");
    }
}
