package com.maayn.financialservice.strategy.certificate.impl;

import com.maayn.financialservice.domain.certificate.WithdrawalResult;
import com.maayn.financialservice.strategy.certificate.CertificateLiquidityStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component("certificateLockedLiquidityStrategy")
public class LockedLiquidityStrategy implements CertificateLiquidityStrategy {
    @Override
    public WithdrawalResult withdraw(UUID certificateId, BigDecimal principal, BigDecimal accruedInterest, BigDecimal requestedAmount, BigDecimal penaltyRate, LocalDate date) {
        return new WithdrawalResult(certificateId, requestedAmount, BigDecimal.ZERO, BigDecimal.ZERO, "REJECTED", "Certificate is locked until maturity");
    }
}
