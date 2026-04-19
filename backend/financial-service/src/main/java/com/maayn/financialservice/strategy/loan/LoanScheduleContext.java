package com.maayn.financialservice.strategy.loan;

import com.maayn.financialservice.domain.loan.LoanInterestMethod;
import com.maayn.financialservice.domain.loan.LoanRateMode;
import com.maayn.financialservice.domain.loan.LoanRepaymentMethod;
import com.maayn.financialservice.domain.loan.RateChange;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record LoanScheduleContext(
        UUID loanId,
        BigDecimal principal,
        BigDecimal annualRate,
        Integer tenureMonths,
        LocalDate startDate,
        LocalDate maturityDate,
        LoanInterestMethod interestMethod,
        LoanRateMode rateMode,
        LoanRepaymentMethod repaymentMethod,
        BigDecimal monthlyFee,
        BigDecimal penaltyRate,
        List<RateChange> rateHistory
) {
}
