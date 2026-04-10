package com.maayn.transactionservice.service;

import com.maayn.transactionservice.entity.LedgerBalance;
import com.maayn.transactionservice.mappers.LedgerMapper;
import com.maayn.transactionservice.repository.LedgerBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.errors.LedgerErrors;
import maayn.veld.generated.models.ledger.BalanceResponse;
import maayn.veld.generated.services.ILedgerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerService implements ILedgerService {

    private final LedgerBalanceRepository ledgerBalanceRepository;

    @Transactional(readOnly = true)
    @Override
    public BalanceResponse getAccountBalance(String accountId) throws Exception {
        log.info("Fetching real-time ledger balance for account {}", accountId);

        UUID accountIdAsUUID = parseAccountId(accountId);

        LedgerBalance balance = ledgerBalanceRepository.getLedgerBalanceByAccountId(accountIdAsUUID)
                .orElseThrow(() -> LedgerErrors.GetAccountBalanceErrors.accountLedgerNotInitialized(
                        "Ledger balance not initialized for account: " + accountId
                ));
        
        return LedgerMapper.toBalanceResponse(balance);
    }
    
    @Transactional(propagation = Propagation.MANDATORY)
    public void executeTransferMath(UUID sourceId, UUID destId, BigDecimal amount) {
        LedgerBalance source = getOrCreateBalance(sourceId);
        LedgerBalance dest = getOrCreateBalance(destId);

        source.debit(amount);
        dest.credit(amount);

        ledgerBalanceRepository.saveAll(List.of(source, dest));
    }
    
    private UUID parseAccountId(String accountId) {
        try {
            return UUID.fromString(accountId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format for account ID: " + accountId);
        }
    }

    private LedgerBalance getOrCreateBalance(UUID accountId) {
        return ledgerBalanceRepository.getLedgerBalanceByAccountId(accountId)
                .orElse(new LedgerBalance(accountId));
    }
}