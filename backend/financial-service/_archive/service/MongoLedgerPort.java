package com.maayn.financialservice.service;

import com.maayn.financialservice.entity.LedgerEntryDocument;
import com.maayn.financialservice.repo.LedgerEntryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MongoLedgerPort implements LedgerPort {

    private final LedgerEntryRepo ledgerEntryRepo;

    @Override
    public void recordLoanEntry(UUID loanId, String eventType, BigDecimal amount, String debitAccount, String creditAccount, String description) {
        ledgerEntryRepo.save(build("LOAN", loanId, eventType, amount, debitAccount, creditAccount, description));
    }

    @Override
    public void recordCertificateEntry(UUID certificateId, String eventType, BigDecimal amount, String debitAccount, String creditAccount, String description) {
        ledgerEntryRepo.save(build("CERTIFICATE", certificateId, eventType, amount, debitAccount, creditAccount, description));
    }

    private LedgerEntryDocument build(String sourceType, UUID sourceId, String eventType, BigDecimal amount, String debitAccount, String creditAccount, String description) {
        LedgerEntryDocument entry = new LedgerEntryDocument();
        entry.setId(UUID.randomUUID());
        entry.setSourceType(sourceType);
        entry.setSourceId(sourceId);
        entry.setEventType(eventType);
        entry.setAmount(amount);
        entry.setDebitAccount(debitAccount);
        entry.setCreditAccount(creditAccount);
        entry.setCurrency("USD");
        entry.setDescription(description);
        entry.setCreatedAt(LocalDateTime.now());
        return entry;
    }
}
