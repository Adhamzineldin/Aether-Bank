package com.maayn.financialservice.strategy.certificate;

import com.maayn.financialservice.domain.certificate.WithdrawalResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface CertificateLiquidityStrategy {
    WithdrawalResult withdraw(UUID certificateId, BigDecimal principal, BigDecimal accruedInterest, BigDecimal requestedAmount, BigDecimal penaltyRate, LocalDate date);
}
