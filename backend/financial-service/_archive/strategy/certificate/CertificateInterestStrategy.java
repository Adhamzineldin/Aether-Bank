package com.maayn.financialservice.strategy.certificate;

import com.maayn.financialservice.domain.certificate.InterestAccrual;
import com.maayn.financialservice.domain.certificate.RateChange;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CertificateInterestStrategy {
    InterestAccrual accrue(BigDecimal principal, BigDecimal annualRate, LocalDate from, LocalDate to, List<RateChange> rateHistory);
}
