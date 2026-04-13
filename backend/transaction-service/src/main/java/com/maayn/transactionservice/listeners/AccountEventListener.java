package com.maayn.transactionservice.listeners;

import com.maayn.transactionservice.entity.LedgerAccountId;
import com.maayn.transactionservice.entity.LedgerBalance;
import com.maayn.transactionservice.repository.LedgerBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.constants.TransactionRabbitConfig;
import maayn.veld.generated.sdk.account.models.shared.AccountCreatedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventListener {

    private final LedgerBalanceRepository ledgerBalanceRepository;

    @RabbitListener(queues = TransactionRabbitConfig.ACCOUNT_EVENTS_QUEUE)
    @Transactional
    public void handleAccountCreated(AccountCreatedEvent event) {
        
        String requestedCurrency = event.getCurrency();

        log.info("Received AccountCreatedEvent for Account ID: {} with Base Currency: {}",
                event.getAccountId(), requestedCurrency);

        LedgerAccountId id = new LedgerAccountId(event.getAccountId(), requestedCurrency);

        boolean exists = ledgerBalanceRepository.existsById(id);

        if (!exists) {
            LedgerBalance newBalance = new LedgerBalance(event.getAccountId(), requestedCurrency);
            ledgerBalanceRepository.save(newBalance);

            log.info("Successfully initialized 0.00 {} Ledger Balance for new account.", requestedCurrency);
        } else {
            log.warn("Wallet {} already exists for account {}", requestedCurrency, event.getAccountId());
        }
    }
}