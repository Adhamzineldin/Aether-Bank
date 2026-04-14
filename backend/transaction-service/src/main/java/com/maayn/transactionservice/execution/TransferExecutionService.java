package com.maayn.transactionservice.execution;

import com.maayn.transactionservice.entity.Transaction;
import com.maayn.transactionservice.exceptions.InvalidBalanceException;
import com.maayn.transactionservice.exceptions.LedgerNotInitializedException;
import com.maayn.transactionservice.service.LedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.errors.TransactionErrors;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferExecutionService {

    private final LedgerService ledgerService;

    public void execute(Transaction transaction) {
        try {
            if (isFxTransfer(transaction)) {
                executeFx(transaction);
            } else {
                executeSameCurrency(transaction);
            }
        } catch (Exception e) {
            throw mapLedgerException(e);
        }
    }

    private boolean isFxTransfer(Transaction transaction) {
        return !transaction.getSourceCurrency()
                .equals(transaction.getDestinationCurrency());
    }

    private void executeFx(Transaction transaction) {
        log.info("Executing FX transfer");

        ledgerService.executeFxTransferMath(
                transaction.getSourceAccountId(),
                transaction.getSourceCurrency(),
                transaction.getAmount(),
                transaction.getDestinationAccountId(),
                transaction.getDestinationCurrency(),
                transaction.getDestinationAmount()
        );
    }

    private void executeSameCurrency(Transaction transaction) {
        log.info("Executing same-currency transfer");

        ledgerService.executeTransferMath(
                transaction.getSourceAccountId(),
                transaction.getDestinationAccountId(),
                transaction.getAmount(),
                transaction.getSourceCurrency()
        );
    }

    private RuntimeException mapLedgerException(Exception e) {
        if (e instanceof LedgerNotInitializedException) {
            return TransactionErrors.TransferErrors.invalidTarget(
                    "Transfer failed: One or both wallets do not exist in the ledger."
            );
        }

        if (e instanceof InvalidBalanceException) {
            return TransactionErrors.TransferErrors.insufficientFunds(
                    "Transfer failed: Insufficient funds in source wallet."
            );
        }

        return new RuntimeException(e);
    }
}