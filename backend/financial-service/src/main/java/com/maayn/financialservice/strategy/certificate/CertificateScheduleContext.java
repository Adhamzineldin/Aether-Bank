package com.maayn.financialservice.strategy.certificate;

import com.maayn.financialservice.domain.certificate.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CertificateScheduleContext(
        UUID certificateId,
        BigDecimal principal,
        BigDecimal annualRate,
        Integer termDays,
        LocalDate issueDate,
        LocalDate maturityDate,
        CertificateInterestMethod interestMethod,
        PayoutMethod payoutMethod,
        LiquidityMethod liquidityMethod,
        RateBehaviorMethod rateBehaviorMethod,
        BigDecimal payoutIntervalDays,
        BigDecimal penaltyRate,
        List<RateChange> rateHistory
) {
}
