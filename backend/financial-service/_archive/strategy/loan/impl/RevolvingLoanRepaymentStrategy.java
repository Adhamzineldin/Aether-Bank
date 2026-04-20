package com.maayn.financialservice.strategy.loan.impl;

import com.maayn.financialservice.domain.common.FinancialMath;
import com.maayn.financialservice.domain.loan.Payment;
import com.maayn.financialservice.domain.loan.RepaymentSchedule;
import com.maayn.financialservice.domain.loan.RepaymentScheduleLine;
import com.maayn.financialservice.strategy.loan.LoanRepaymentStrategy;
import com.maayn.financialservice.strategy.loan.LoanScheduleContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component("loanRevolvingRepaymentStrategy")
public class RevolvingLoanRepaymentStrategy implements LoanRepaymentStrategy {
    @Override
    public RepaymentSchedule buildSchedule(LoanScheduleContext context) {
        LocalDate due = context.startDate().plusMonths(1);
        RepaymentScheduleLine line = new RepaymentScheduleLine(
                due,
                FinancialMath.money(context.principal()),
                FinancialMath.money(BigDecimal.ZERO),
                FinancialMath.money(context.principal().multiply(context.annualRate()).divide(BigDecimal.valueOf(12), FinancialMath.MC)),
                FinancialMath.money(context.monthlyFee()),
                FinancialMath.money(context.monthlyFee()),
                FinancialMath.money(context.principal()),
                "REVOLVING"
        );
        return new RepaymentSchedule(context.loanId(), List.of(line));
    }

    @Override
    public Payment allocatePayment(LoanScheduleContext context, Payment payment) {
        BigDecimal principal = payment.amount().subtract(payment.feePortion() == null ? BigDecimal.ZERO : payment.feePortion())
                .subtract(payment.penaltyPortion() == null ? BigDecimal.ZERO : payment.penaltyPortion())
                .subtract(payment.interestPortion() == null ? BigDecimal.ZERO : payment.interestPortion());
        return new Payment(payment.paymentId(), payment.loanId(), FinancialMath.money(payment.amount()), FinancialMath.money(principal), payment.interestPortion(), payment.feePortion(), payment.penaltyPortion(), payment.paymentDate(), payment.reference(), "ALLOCATED");
    }
}
