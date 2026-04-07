package com.maayn.transactionservice.validators;

import com.maayn.transactionservice.entity.Transaction;
import maayn.veld.generated.errors.TransactionErrors; 
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class TransactionValidator {

    public void validateTransfer(Transaction transaction) {
        
        isLessThanOrEqualToZero(transaction);
        
        isSourceEqualToDestination(transaction);

        // Rule 3: Minimum Balance (Future proofing)
        // You could add a check here to ensure the amount isn't suspiciously high
    }
    
    private void isLessThanOrEqualToZero(Transaction transaction) {
        if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw TransactionErrors.TransferErrors.invalidAmount(
                    "Transferred amount must be greater than 0"
            );
        }
    }
    
    private void isSourceEqualToDestination(Transaction transaction) {
        if (transaction.getSourceAccountId().equals(transaction.getDestinationAccountId())) {
            throw TransactionErrors.TransferErrors.invalidTarget(
                    "Source and Destination accounts cannot be the same"
            );
        }
    }
    
    
    
}