package com.maayn.financialservice.strategy.certificate.impl;

import com.maayn.financialservice.domain.certificate.RateChange;
import com.maayn.financialservice.strategy.certificate.RateBehaviorStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Component("certificateIndexedRateBehaviorStrategy")
public class IndexedRateBehaviorStrategy implements RateBehaviorStrategy {
    @Override
    public BigDecimal resolveAnnualRate(BigDecimal baseRate, LocalDate date, List<RateChange> history) {
        if (history == null || history.isEmpty()) {
            return baseRate;
        }
        return history.stream()
                .filter(change -> !change.effectiveDate().isAfter(date))
                .max(Comparator.comparing(RateChange::effectiveDate))
                .map(RateChange::annualRate)
                .orElse(baseRate);
    }
}
