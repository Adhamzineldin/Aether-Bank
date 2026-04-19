package com.maayn.financialservice.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface LedgerPort {
    void recordLoanEntry(UUID loanId, String eventType, BigDecimal amount, String debitAccount, String creditAccount, String description);
    void recordCertificateEntry(UUID certificateId, String eventType, BigDecimal amount, String debitAccount, String creditAccount, String description);
}
