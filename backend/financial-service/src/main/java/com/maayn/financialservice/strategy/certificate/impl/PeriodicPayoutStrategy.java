package com.maayn.financialservice.strategy.certificate.impl;

import com.maayn.financialservice.domain.common.FinancialMath;
import com.maayn.financialservice.domain.certificate.PayoutLine;
import com.maayn.financialservice.domain.certificate.PayoutSchedule;
import com.maayn.financialservice.strategy.certificate.CertificatePayoutStrategy;
import com.maayn.financialservice.strategy.certificate.CertificateScheduleContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component("certificatePeriodicPayoutStrategy")
public class PeriodicPayoutStrategy implements CertificatePayoutStrategy {
    @Override
    public PayoutSchedule buildSchedule(CertificateScheduleContext context) {
        List<PayoutLine> lines = new ArrayList<>();
        int periods = Math.max(context.termDays() / Math.max(context.payoutIntervalDays().intValue(), 1), 1);
        BigDecimal periodicInterest = context.principal()
                .multiply(context.annualRate())
                .multiply(context.payoutIntervalDays())
                .divide(BigDecimal.valueOf(365), FinancialMath.MC);
        LocalDate dueDate = context.issueDate();
        for (int i = 1; i <= periods; i++) {
            dueDate = dueDate.plusDays(context.payoutIntervalDays().longValue());
            BigDecimal principalReturn = i == periods ? context.principal() : BigDecimal.ZERO;
            BigDecimal total = principalReturn.add(periodicInterest);
            lines.add(new PayoutLine(dueDate, FinancialMath.money(principalReturn), FinancialMath.money(periodicInterest), FinancialMath.money(total), "SCHEDULED"));
        }
        return new PayoutSchedule(context.certificateId(), lines);
    }
}
