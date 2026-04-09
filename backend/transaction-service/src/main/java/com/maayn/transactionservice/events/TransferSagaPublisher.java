package com.maayn.transactionservice.events;

import com.maayn.transactionservice.entity.Transaction;
import com.maayn.transactionservice.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.TransferInitiatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferSagaPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void initiateTransferSaga(Transaction transaction) {
        TransferInitiatedEvent sagaEvent = new TransferInitiatedEvent(
                transaction.getReferenceNumber(),
                transaction.getSourceAccountId(),
                transaction.getDestinationAccountId(),
                transaction.getAmount(),
                transaction.getCurrency()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SAGA_EXCHANGE,
                RabbitMQConfig.TRANSFER_INITIATED_ROUTING_KEY,
                sagaEvent
        );

        log.info("SAGA Initiated: TransferInitiatedEvent sent for TXN {}", transaction.getReferenceNumber());
    }
}