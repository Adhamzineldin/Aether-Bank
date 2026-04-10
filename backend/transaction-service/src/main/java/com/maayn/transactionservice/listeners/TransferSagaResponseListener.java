package com.maayn.transactionservice.listeners;

import com.maayn.transactionservice.config.RabbitMQConfig;
import com.maayn.transactionservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.sdk.account.models.shared.FundsSecuredEvent;

import maayn.veld.generated.models.transaction.TransactionStatus;
import maayn.veld.generated.sdk.account.models.shared.InsufficientFundsEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferSagaResponseListener {

    private final TransactionService transactionService;

    @RabbitListener(queues = RabbitMQConfig.SAGA_SUCCESS_QUEUE)
    public void handleSuccess(FundsSecuredEvent event) {
        log.info("SAGA Callback: Success received for TXN {}", event.getTransactionId());
        transactionService.finalizeTransaction(event.getTransactionId(), TransactionStatus.SUCCESS, "Funds secured");
    }

    @RabbitListener(queues = RabbitMQConfig.SAGA_FAILURE_QUEUE)
    public void handleFailure(InsufficientFundsEvent event) {
        log.warn("SAGA Callback: Failure received for TXN {} - Reason: {}", event.getTransactionId(), event.getReason());
        transactionService.finalizeTransaction(event.getTransactionId(), TransactionStatus.FAILED, event.getReason());
    }
}