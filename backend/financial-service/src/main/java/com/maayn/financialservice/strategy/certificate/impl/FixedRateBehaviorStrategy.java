package com.maayn.financialservice.strategy.certificate.impl;

import com.maayn.financialservice.domain.certificate.RateChange;
import com.maayn.financialservice.strategy.certificate.RateBehaviorStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component("certificateFixedRateBehaviorStrategy")
public class FixedRateBehaviorStrategy implements RateBehaviorStrategy {
    @Override
    public BigDecimal resolveAnnualRate(BigDecimal baseRate, LocalDate date, List<RateChange> history) {
        return baseRate;
    }
}
