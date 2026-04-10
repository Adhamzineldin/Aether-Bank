package com.maayn.transactionservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import maayn.veld.generated.errors.TransactionErrors;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "ledger_balances")
@Getter
@Setter
@NoArgsConstructor
public class LedgerBalance {

    @Id
    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    @Column(name = "available_balance", nullable = false)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private String currency;


    @Column(name = "pending_holds", nullable = false)
    private BigDecimal pendingHolds = BigDecimal.ZERO;

    @Version
    private Long version;

    public LedgerBalance(UUID accountId) {
        this.accountId = accountId;
    }
    
    public void debit(BigDecimal amount) {
        if (this.availableBalance.compareTo(amount) < 0) {
            throw TransactionErrors.TransferErrors.insufficientFunds("Insufficient funds in account: " + this.accountId);
        }
        this.availableBalance = this.availableBalance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        this.availableBalance = this.availableBalance.add(amount);
    }
}