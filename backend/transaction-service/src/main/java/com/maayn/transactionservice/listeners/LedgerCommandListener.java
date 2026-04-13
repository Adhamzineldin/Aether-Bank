package com.maayn.transactionservice.listeners;

import com.maayn.transactionservice.events.TransactionEventPublisher;
import com.maayn.transactionservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.constants.TransactionRabbitConfig;
import maayn.veld.generated.models.transaction.TransferRequest;
import maayn.veld.generated.sdk.notification.models.shared.TransferFailedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class LedgerCommandListener {

    private final TransactionService transactionService;
    private final TransactionEventPublisher eventPublisher;

    @RabbitListener(queues = TransactionRabbitConfig.SAGA_COMMANDS_QUEUE)
    public void handleExecuteTransferCommand(TransferRequest command) {
        log.info("Received SAGA Transfer Command. IdempotencyKey: {}", command.getIdempotencyKey());

        try {
            transactionService.transfer(command);
        } catch (Exception e) {
            log.error("SAGA Transfer Command failed for Key {}: {}", command.getIdempotencyKey(), e.getMessage());
            dispatchFailureResponse(command, e.getMessage());
        }
    }
    
    private void dispatchFailureResponse(TransferRequest command, String errorMessage) {
        TransferFailedEvent failedEvent = new TransferFailedEvent();

        failedEvent.setReferenceNumber(command.getIdempotencyKey());
        failedEvent.setSourceAccountId(command.getSourceAccountId());
        failedEvent.setDestinationAccountId(command.getDestinationAccountId());
        failedEvent.setAmount(command.getAmount());
        failedEvent.setCurrency(command.getCurrency());
        failedEvent.setFailureReason(errorMessage);
        failedEvent.setEventTime(LocalDateTime.now());

        eventPublisher.publish(failedEvent);
    }
}