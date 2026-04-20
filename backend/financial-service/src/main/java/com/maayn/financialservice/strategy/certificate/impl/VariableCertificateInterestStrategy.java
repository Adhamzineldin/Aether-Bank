package com.maayn.financialservice.strategy.certificate.impl;

import com.maayn.financialservice.domain.common.FinancialMath;
import com.maayn.financialservice.domain.certificate.InterestAccrual;
import com.maayn.financialservice.domain.certificate.RateChange;
import com.maayn.financialservice.strategy.certificate.CertificateInterestStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Component("certificateVariableInterestStrategy")
public class VariableCertificateInterestStrategy implements CertificateInterestStrategy {
    @Override
    public InterestAccrual accrue(BigDecimal principal, BigDecimal annualRate, LocalDate from, LocalDate to, List<RateChange> rateHistory) {
        long days = ChronoUnit.DAYS.between(from, to);
        BigDecimal effectiveRate = resolveRate(annualRate, from, rateHistory);
        BigDecimal interest = FinancialMath.money(principal
                .multiply(effectiveRate)
                .multiply(BigDecimal.valueOf(days))
                .divide(BigDecimal.valueOf(365), FinancialMath.MC));
        return new InterestAccrual(from, to, days, effectiveRate, FinancialMath.money(principal), interest, false, "Variable accrual");
    }

    private BigDecimal resolveRate(BigDecimal baseRate, LocalDate date, List<RateChange> rateHistory) {
        if (rateHistory == null || rateHistory.isEmpty()) {
            return baseRate;
        }
        return rateHistory.stream()
                .filter(change -> !change.effectiveDate().isAfter(date))
                .max(Comparator.comparing(RateChange::effectiveDate))
                .map(RateChange::annualRate)
                .orElse(baseRate);
    }
}
