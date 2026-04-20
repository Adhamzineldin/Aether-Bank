package com.maayn.financialservice.domain.loan;

import java.util.List;
import java.util.UUID;

public record RepaymentSchedule(
        UUID loanId,
        List<RepaymentScheduleLine> lines
) {
}
