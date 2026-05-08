package com.maayn.transactionservice.entity;

import com.maayn.transactionservice.exceptions.InvalidBalanceException;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class LedgerBalance {

    @EmbeddedId
    private LedgerAccountId id;

    private BigDecimal availableBalance = BigDecimal.ZERO;
    private BigDecimal pendingHolds = BigDecimal.ZERO;

    @Version
    private Long version;
    
    public LedgerBalance(UUID accountId, String currency) {
        this.id = new LedgerAccountId(accountId, currency);
    }

    // OCL: LB_01_AvailableBalanceNonNegative — credit never causes a negative balance
    public void credit(BigDecimal amount) {
        this.availableBalance = this.availableBalance.add(amount);
    }

    // OCL: LB_01_AvailableBalanceNonNegative — debit enforces non-negative balance invariant
    public void debit(BigDecimal amount) {
        if (this.availableBalance.compareTo(amount) < 0) {
            throw new InvalidBalanceException("Insufficient funds in " + id.getCurrency() + " wallet for account: " + id.getAccountId());
        }
        this.availableBalance = this.availableBalance.subtract(amount);
    }
}