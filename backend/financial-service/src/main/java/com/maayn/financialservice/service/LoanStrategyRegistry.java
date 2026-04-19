package com.maayn.financialservice.service;

import com.maayn.financialservice.domain.loan.LoanInterestMethod;
import com.maayn.financialservice.domain.loan.LoanRateMode;
import com.maayn.financialservice.domain.loan.LoanRepaymentMethod;
import com.maayn.financialservice.strategy.loan.LoanInterestStrategy;
import com.maayn.financialservice.strategy.loan.LoanRateStrategy;
import com.maayn.financialservice.strategy.loan.LoanRepaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class LoanStrategyRegistry {

    private final Map<String, LoanInterestStrategy> interestStrategies;
    private final Map<String, LoanRateStrategy> rateStrategies;
    private final Map<String, LoanRepaymentStrategy> repaymentStrategies;

    public LoanInterestStrategy resolveInterestStrategy(LoanInterestMethod method) {
        return switch (method) {
            case SIMPLE -> interestStrategies.get("loanSimpleInterestStrategy");
            case COMPOUND -> interestStrategies.get("loanCompoundInterestStrategy");
            case AMORTIZED -> interestStrategies.get("loanAmortizedInterestStrategy");
            case INTEREST_ONLY -> interestStrategies.get("loanInterestOnlyStrategy");
        };
    }

    public LoanRateStrategy resolveRateStrategy(LoanRateMode mode) {
        return switch (mode) {
            case FIXED -> rateStrategies.get("loanFixedRateStrategy");
            case VARIABLE -> rateStrategies.get("loanVariableRateStrategy");
        };
    }

    public LoanRepaymentStrategy resolveRepaymentStrategy(LoanRepaymentMethod method) {
        return switch (method) {
            case INSTALLMENT -> repaymentStrategies.get("loanInstallmentRepaymentStrategy");
            case REVOLVING -> repaymentStrategies.get("loanRevolvingRepaymentStrategy");
            case BULLET -> repaymentStrategies.get("loanBulletRepaymentStrategy");
        };
    }
}
