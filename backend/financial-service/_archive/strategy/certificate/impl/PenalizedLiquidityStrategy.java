package com.maayn.financialservice.strategy.certificate.impl;

import com.maayn.financialservice.domain.certificate.WithdrawalResult;
import com.maayn.financialservice.strategy.certificate.CertificateLiquidityStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component("certificatePenalizedLiquidityStrategy")
public class PenalizedLiquidityStrategy implements CertificateLiquidityStrategy {
    @Override
    public WithdrawalResult withdraw(UUID certificateId, BigDecimal principal, BigDecimal accruedInterest, BigDecimal requestedAmount, BigDecimal penaltyRate, LocalDate date) {
        BigDecimal penalty = requestedAmount.multiply(penaltyRate == null ? BigDecimal.ZERO : penaltyRate);
        BigDecimal net = requestedAmount.subtract(penalty);
        return new WithdrawalResult(certificateId, requestedAmount, penalty, net, "APPROVED_WITH_PENALTY", "Early withdrawal penalty applied");
    }
}
