package com.maayn.transactionservice.validators;

import com.maayn.transactionservice.config.Keys;
import com.maayn.transactionservice.entity.Transaction;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.errors.TransactionErrors;
import maayn.veld.generated.sdk.account.AccountClient; // Using the Veld SDK
import maayn.veld.generated.sdk.account.errors.SdkApiError;
import maayn.veld.generated.sdk.account.errors.AccountErrors;
import maayn.veld.generated.sdk.account.models.account.AccountBalanceResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TransactionValidator {

    private final AccountClient accountClient;

    public void validateTransfer(Transaction transaction) throws Exception {
        validatePositiveAmount(transaction);
        validateDifferentAccounts(transaction);
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

    private void validateDestinationAccountExists(Transaction transaction) throws Exception {
        try {
//            boolean accountExists = accountClient.account.doesAccountExist(
//                    String.valueOf(transaction.getDestinationAccountId())
//            );

            //TODO: replace with actual call to account service
            boolean accountExists = true;

            if (!accountExists) {
                throw TransactionErrors.TransferErrors.invalidTarget("Destination account not found");
            }
        } catch (SdkApiError e) {
            throw e;
        }
    }

    private void validateSufficientBalance(Transaction transaction) throws Exception {
        try {
//            AccountBalanceResponse balanceResponse = accountClient.account.getBalance(
//                    String.valueOf(transaction.getSourceAccountId())
//            );

            //TODO: replace with actual call to account service
            AccountBalanceResponse balanceResponse = new AccountBalanceResponse(
                    Keys.getSystemUserId(), new BigDecimal("1000.00"), "EGP");

            if (balanceResponse.getBalance().compareTo(transaction.getAmount()) < 0) {
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