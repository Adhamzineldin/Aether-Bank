package com.maayn.transactionservice.validators;

import com.maayn.transactionservice.entity.Transaction;
import maayn.veld.generated.errors.TransactionErrors;
import maayn.veld.generated.sdk.account.errors.SdkApiError;
import maayn.veld.generated.sdk.account.models.account.AccountBalanceResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import maayn.veld.generated.sdk.account.AccountClient;
import maayn.veld.generated.sdk.account.errors.AccountErrors;

@Component
public class TransactionValidator {

    public void validateTransfer(Transaction transaction) throws Exception {
        validatePositiveAmount(transaction);
        validateDifferentAccounts(transaction);
        validateSourceAccountExists(transaction);
        validateSufficientBalance(transaction);
        validateDestinationAccountExists(transaction);
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

    private void validateSourceAccountExists(Transaction transaction) {

    }

    private void validateDestinationAccountExists(Transaction transaction) throws Exception {
       
        

    }

    private void validateSufficientBalance(Transaction transaction) throws Exception {
        AccountClient accountClient = new AccountClient();
        try {
            AccountBalanceResponse balance = accountClient.account.getBalance(
                    String.valueOf(transaction.getSourceAccountId())
            );

            if (balance.getBalance().compareTo(transaction.getAmount()) < 0) {
                throw TransactionErrors.TransferErrors.insufficientFunds("Insufficient funds in source account");
            }
        } catch (SdkApiError e) {
            if (AccountErrors.getBalance.accountNotFoundError(e)) {
                throw TransactionErrors.TransferErrors.invalidTarget("Source account not found");
            }
            throw e;
        }
    }


}