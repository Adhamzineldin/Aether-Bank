package com.maayn.financialservice.strategy.loan;

import com.maayn.financialservice.domain.loan.Payment;
import com.maayn.financialservice.domain.loan.RepaymentSchedule;

public interface LoanRepaymentStrategy {
    RepaymentSchedule buildSchedule(LoanScheduleContext context);
    Payment allocatePayment(LoanScheduleContext context, Payment payment);
}
