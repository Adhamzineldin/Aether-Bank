package com.maayn.financialservice.strategy.certificate.impl;

import com.maayn.financialservice.domain.common.FinancialMath;
import com.maayn.financialservice.domain.certificate.InterestAccrual;
import com.maayn.financialservice.domain.certificate.RateChange;
import com.maayn.financialservice.strategy.certificate.CertificateInterestStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component("certificateDiscountInterestStrategy")
public class DiscountCertificateInterestStrategy implements CertificateInterestStrategy {
    @Override
    public InterestAccrual accrue(BigDecimal principal, BigDecimal annualRate, LocalDate from, LocalDate to, List<RateChange> rateHistory) {
        long days = ChronoUnit.DAYS.between(from, to);
        BigDecimal discountedValue = principal.divide(BigDecimal.ONE.add(annualRate), FinancialMath.MC);
        BigDecimal interest = FinancialMath.money(principal.subtract(discountedValue));
        return new InterestAccrual(from, to, days, annualRate, FinancialMath.money(principal), interest, false, "Discount/zero-coupon accrual");
    }
}
