package com.maayn.financialservice.strategy.loan.impl;

import com.maayn.financialservice.domain.common.FinancialMath;
import com.maayn.financialservice.domain.loan.Payment;
import com.maayn.financialservice.domain.loan.RepaymentSchedule;
import com.maayn.financialservice.domain.loan.RepaymentScheduleLine;
import com.maayn.financialservice.strategy.loan.LoanRepaymentStrategy;
import com.maayn.financialservice.strategy.loan.LoanScheduleContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component("loanBulletRepaymentStrategy")
public class BulletLoanRepaymentStrategy implements LoanRepaymentStrategy {
    @Override
    public RepaymentSchedule buildSchedule(LoanScheduleContext context) {
        BigDecimal interest = context.principal().multiply(context.annualRate())
                .multiply(BigDecimal.valueOf(context.tenureMonths()))
                .divide(BigDecimal.valueOf(12), FinancialMath.MC);
        RepaymentScheduleLine line = new RepaymentScheduleLine(
                context.maturityDate(),
                FinancialMath.money(context.principal()),
                FinancialMath.money(context.principal()),
                FinancialMath.money(interest),
                FinancialMath.money(context.monthlyFee()),
                FinancialMath.money(context.principal().add(interest).add(context.monthlyFee())),
                BigDecimal.ZERO.setScale(FinancialMath.SCALE),
                "BULLET"
        );
        return new RepaymentSchedule(context.loanId(), List.of(line));
    }

    @Override
    public Payment allocatePayment(LoanScheduleContext context, Payment payment) {
        BigDecimal principal = payment.amount()
                .subtract(payment.interestPortion() == null ? BigDecimal.ZERO : payment.interestPortion())
                .subtract(payment.feePortion() == null ? BigDecimal.ZERO : payment.feePortion())
                .subtract(payment.penaltyPortion() == null ? BigDecimal.ZERO : payment.penaltyPortion());
        return new Payment(payment.paymentId(), payment.loanId(), FinancialMath.money(payment.amount()), FinancialMath.money(principal), payment.interestPortion(), payment.feePortion(), payment.penaltyPortion(), payment.paymentDate(), payment.reference(), "ALLOCATED");
    }
}
