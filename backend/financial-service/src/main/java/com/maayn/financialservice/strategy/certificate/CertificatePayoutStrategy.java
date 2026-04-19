package com.maayn.financialservice.strategy.certificate;

import com.maayn.financialservice.domain.certificate.PayoutSchedule;

public interface CertificatePayoutStrategy {
    PayoutSchedule buildSchedule(CertificateScheduleContext context);
}
