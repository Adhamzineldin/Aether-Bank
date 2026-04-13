package com.maayn.transactionservice.validators;

import com.maayn.transactionservice.entity.Transaction;
import maayn.veld.generated.errors.TransactionErrors;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransactionValidator {

    public void validateTransfer(Transaction transaction) {
        validatePositiveAmount(transaction);
        validateDifferentAccounts(transaction);
    }

    private void validatePositiveAmount(Transaction transaction) {
        if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw TransactionErrors.TransferErrors.invalidAmount("Transferred amount must be greater than 0");
        }
    }

    private void validateDifferentAccounts(Transaction transaction) {
        if (transaction.getSourceAccountId().equals(transaction.getDestinationAccountId())) {
            throw TransactionErrors.TransferErrors.invalidTarget("Source and Destination accounts cannot be the same");
        }
    }
}