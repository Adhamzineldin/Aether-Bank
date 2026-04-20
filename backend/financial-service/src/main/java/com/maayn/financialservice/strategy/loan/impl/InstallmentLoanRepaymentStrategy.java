package com.maayn.financialservice.strategy.loan.impl;

import com.maayn.financialservice.domain.common.FinancialMath;
import com.maayn.financialservice.domain.loan.Payment;
import com.maayn.financialservice.domain.loan.RepaymentSchedule;
import com.maayn.financialservice.domain.loan.RepaymentScheduleLine;
import com.maayn.financialservice.strategy.loan.LoanRepaymentStrategy;
import com.maayn.financialservice.strategy.loan.LoanScheduleContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component("loanInstallmentRepaymentStrategy")
public class InstallmentLoanRepaymentStrategy implements LoanRepaymentStrategy {

    @Override
    public RepaymentSchedule buildSchedule(LoanScheduleContext context) {
        List<RepaymentScheduleLine> lines = new ArrayList<>();
        BigDecimal monthlyRate = context.annualRate().divide(BigDecimal.valueOf(12), FinancialMath.MC);
        int months = Math.max(context.tenureMonths(), 1);
        BigDecimal emi = calculateEmi(context.principal(), monthlyRate, months).setScale(FinancialMath.SCALE, RoundingMode.HALF_UP);
        BigDecimal balance = context.principal();
        LocalDate dueDate = context.startDate();

        for (int i = 1; i <= months; i++) {
            dueDate = dueDate.plusMonths(1);
            BigDecimal interest = FinancialMath.money(balance.multiply(monthlyRate));
            BigDecimal principal = FinancialMath.money(emi.subtract(interest));
            if (i == months) {
                principal = balance;
                emi = FinancialMath.money(principal.add(interest));
            }
            BigDecimal closing = FinancialMath.money(balance.subtract(principal));
            lines.add(new RepaymentScheduleLine(dueDate, FinancialMath.money(balance), principal, interest, FinancialMath.money(context.monthlyFee()), FinancialMath.money(emi.add(context.monthlyFee())), closing, "SCHEDULED"));
            balance = closing;
        }
        return new RepaymentSchedule(context.loanId(), lines);
    }

    @Override
    public Payment allocatePayment(LoanScheduleContext context, Payment payment) {
        BigDecimal amount = payment.amount();
        BigDecimal fees = payment.feePortion() == null ? BigDecimal.ZERO : payment.feePortion();
        BigDecimal penalty = payment.penaltyPortion() == null ? BigDecimal.ZERO : payment.penaltyPortion();
        BigDecimal interest = payment.interestPortion() == null ? BigDecimal.ZERO : payment.interestPortion();
        BigDecimal principal = amount.subtract(fees).subtract(penalty).subtract(interest);
        return new Payment(payment.paymentId(), payment.loanId(), FinancialMath.money(amount), FinancialMath.money(principal), FinancialMath.money(interest), FinancialMath.money(fees), FinancialMath.money(penalty), payment.paymentDate(), payment.reference(), "ALLOCATED");
    }

    private BigDecimal calculateEmi(BigDecimal principal, BigDecimal monthlyRate, int months) {
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(months), FinancialMath.MC);
        }
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal pow = onePlusR.pow(months, FinancialMath.MC);
        return principal.multiply(monthlyRate).multiply(pow).divide(pow.subtract(BigDecimal.ONE), FinancialMath.MC);
    }
}
