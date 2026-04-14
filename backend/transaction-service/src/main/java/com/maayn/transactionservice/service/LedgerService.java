package com.maayn.transactionservice.service;

import com.maayn.transactionservice.entity.LedgerAccountId;
import com.maayn.transactionservice.entity.LedgerBalance;
import com.maayn.transactionservice.exceptions.LedgerNotInitializedException;
import com.maayn.transactionservice.mappers.LedgerMapper;
import com.maayn.transactionservice.repository.LedgerBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.errors.LedgerErrors;
import maayn.veld.generated.models.ledger.BalanceResponse;
import maayn.veld.generated.sdk.account.constants.SystemAccounts;
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
    public BalanceResponse getAccountBalance(String accountId, String currency) throws Exception {
        log.info("Fetching real-time {} ledger balance for account {}", currency, accountId);

        UUID accountIdAsUUID = parseAccountId(accountId);

        LedgerAccountId id = new LedgerAccountId(accountIdAsUUID, currency);

        LedgerBalance balance = ledgerBalanceRepository.findById(id)
                .orElseThrow(() -> LedgerErrors.GetAccountBalanceErrors.accountLedgerNotInitialized(
                        "Ledger balance not initialized for account: " + accountId + " in currency: " + currency
                ));

        return LedgerMapper.toBalanceResponse(balance);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void executeTransferMath(UUID sourceId, UUID destId, BigDecimal amount, String currency) {
        LedgerBalance source = getBalanceOrThrow(sourceId, currency);
        LedgerBalance dest = getBalanceOrThrow(destId, currency);

        source.debit(amount);
        dest.credit(amount);

        ledgerBalanceRepository.saveAll(List.of(source, dest));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void executeFxTransferMath(UUID sourceId, String sourceCurrency, BigDecimal sourceAmount,
                                      UUID destId, String destCurrency, BigDecimal destAmount) {

       
        LedgerBalance sourceUser = getBalanceOrThrow(sourceId, sourceCurrency);
        LedgerBalance fxVaultSource = getBalanceOrThrow(SystemAccounts.FX_MARKET_MAKER_ID, sourceCurrency);

        sourceUser.debit(sourceAmount);
        fxVaultSource.credit(sourceAmount);

        LedgerBalance fxVaultDest = getBalanceOrThrow(SystemAccounts.FX_MARKET_MAKER_ID, destCurrency);
        LedgerBalance destUser = getBalanceOrThrow(destId, destCurrency);

        fxVaultDest.debit(destAmount);
        destUser.credit(destAmount);

        ledgerBalanceRepository.saveAll(List.of(sourceUser, fxVaultSource, fxVaultDest, destUser));
    }
    
    private UUID parseAccountId(String accountId) {
        try {
            return UUID.fromString(accountId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format for account ID: " + accountId);
        }
    }

    private LedgerBalance getBalanceOrThrow(UUID accountId, String currency) {
        LedgerAccountId id = new LedgerAccountId(accountId, currency);
        return ledgerBalanceRepository.findById(id)
                .orElseThrow(() -> new LedgerNotInitializedException(
                        "Ledger missing for Account: " + accountId + " in Currency: " + currency
                ));
    }
    
    
}