package com.maayn.financialservice.service;

import com.maayn.financialservice.domain.certificate.*;
import com.maayn.financialservice.strategy.certificate.CertificateInterestStrategy;
import com.maayn.financialservice.strategy.certificate.CertificateLiquidityStrategy;
import com.maayn.financialservice.strategy.certificate.CertificatePayoutStrategy;
import com.maayn.financialservice.strategy.certificate.RateBehaviorStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CertificateStrategyRegistry {

    private final Map<String, CertificateInterestStrategy> interestStrategies;
    private final Map<String, CertificatePayoutStrategy> payoutStrategies;
    private final Map<String, CertificateLiquidityStrategy> liquidityStrategies;
    private final Map<String, RateBehaviorStrategy> rateBehaviorStrategies;

    public CertificateInterestStrategy resolveInterestStrategy(CertificateInterestMethod method) {
        return switch (method) {
            case SIMPLE -> interestStrategies.get("certificateSimpleInterestStrategy");
            case COMPOUND -> interestStrategies.get("certificateCompoundInterestStrategy");
            case DISCOUNT -> interestStrategies.get("certificateDiscountInterestStrategy");
            case VARIABLE -> interestStrategies.get("certificateVariableInterestStrategy");
        };
    }

    public CertificatePayoutStrategy resolvePayoutStrategy(PayoutMethod method) {
        return switch (method) {
            case PERIODIC -> payoutStrategies.get("certificatePeriodicPayoutStrategy");
            case AT_MATURITY -> payoutStrategies.get("certificateAtMaturityPayoutStrategy");
        };
    }

    public CertificateLiquidityStrategy resolveLiquidityStrategy(LiquidityMethod method) {
        return switch (method) {
            case LOCKED -> liquidityStrategies.get("certificateLockedLiquidityStrategy");
            case BREAKABLE -> liquidityStrategies.get("certificateBreakableLiquidityStrategy");
            case PENALIZED -> liquidityStrategies.get("certificatePenalizedLiquidityStrategy");
        };
    }

    public RateBehaviorStrategy resolveRateBehaviorStrategy(RateBehaviorMethod method) {
        return switch (method) {
            case FIXED -> rateBehaviorStrategies.get("certificateFixedRateBehaviorStrategy");
            case STEP_UP -> rateBehaviorStrategies.get("certificateStepUpRateBehaviorStrategy");
            case INDEXED -> rateBehaviorStrategies.get("certificateIndexedRateBehaviorStrategy");
        };
    }
}
