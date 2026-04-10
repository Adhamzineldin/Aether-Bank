package com.maayn.transactionservice.listeners;

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
        log.info("Received AccountCreatedEvent for Account ID: {}", event.getAccountId());

        boolean exists = ledgerBalanceRepository.existsById(event.getAccountId());

        if (!exists) {
            LedgerBalance newBalance = new LedgerBalance(event.getAccountId());
            ledgerBalanceRepository.save(newBalance);
            log.info("Successfully initialized $0.00 Ledger Balance for new account.");
        }
    }
}