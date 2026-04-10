package com.maayn.transactionservice.service;

import com.maayn.transactionservice.entity.LedgerBalance;
import com.maayn.transactionservice.repository.LedgerBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerBalanceRepository ledgerBalanceRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void executeTransferMath(UUID sourceId, UUID destId, BigDecimal amount) {

        LedgerBalance source = getOrCreateBalance(sourceId);
        LedgerBalance dest = getOrCreateBalance(destId);

        source.debit(amount);
        dest.credit(amount);

        ledgerBalanceRepository.saveAll(List.of(source, dest));
    }

    private LedgerBalance getOrCreateBalance(UUID accountId) {
        return ledgerBalanceRepository.getLedgerBalanceByAccountId(accountId)
                .orElse(new LedgerBalance(accountId));
    }
}