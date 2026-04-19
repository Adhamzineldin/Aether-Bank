package com.maayn.financialservice.strategy.certificate.impl;

import com.maayn.financialservice.domain.common.FinancialMath;
import com.maayn.financialservice.domain.certificate.PayoutLine;
import com.maayn.financialservice.domain.certificate.PayoutSchedule;
import com.maayn.financialservice.strategy.certificate.CertificatePayoutStrategy;
import com.maayn.financialservice.strategy.certificate.CertificateScheduleContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component("certificateAtMaturityPayoutStrategy")
public class AtMaturityPayoutStrategy implements CertificatePayoutStrategy {
    @Override
    public PayoutSchedule buildSchedule(CertificateScheduleContext context) {
        BigDecimal interest = context.principal()
                .multiply(context.annualRate())
                .multiply(BigDecimal.valueOf(context.termDays()))
                .divide(BigDecimal.valueOf(365), FinancialMath.MC);
        return new PayoutSchedule(context.certificateId(), List.of(
                new PayoutLine(context.maturityDate(), FinancialMath.money(context.principal()), FinancialMath.money(interest), FinancialMath.money(context.principal().add(interest)), "SCHEDULED")
        ));
    }
}
