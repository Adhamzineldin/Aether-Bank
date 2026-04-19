package com.maayn.financialservice.strategy.certificate;

import com.maayn.financialservice.domain.certificate.RateChange;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RateBehaviorStrategy {
    BigDecimal resolveAnnualRate(BigDecimal baseRate, LocalDate date, List<RateChange> history);
}
