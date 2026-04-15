package com.maayn.financialservice.service;

import maayn.veld.generated.models.loan.LoanApplication;
import maayn.veld.generated.models.loan.LoanApplicationResponse;
import maayn.veld.generated.services.ILoanService;

import java.util.List;

public class LoanService implements ILoanService {
    @Override
    public LoanApplicationResponse loanSubmit(LoanApplication input) throws Exception {
        return null;
    }

    @Override
    public LoanApplication getLoan(String id) throws Exception {
        return null;
    }

    @Override
    public List<LoanApplication> getCustomerLoans(String customerId) throws Exception {
        return List.of();
    }
}
