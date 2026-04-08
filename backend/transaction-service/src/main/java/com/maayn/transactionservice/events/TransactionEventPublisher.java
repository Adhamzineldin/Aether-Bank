package com.maayn.transactionservice.events;

import com.maayn.transactionservice.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.TransactionEvent;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(TransactionEvent event) {
        CorrelationData correlationData = new CorrelationData(event.getReferenceNumber());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TRANSACTION_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                event,
                correlationData
        );

        correlationData.getFuture().whenComplete((result, ex) ->
                handleBrokerConfirmation(event.getReferenceNumber(), result, ex)
        );
    }

    private void handleBrokerConfirmation(String referenceNumber, CorrelationData.Confirm result, Throwable ex) {
        if (ex != null) {
            log.error("Network error sending message to RabbitMQ for: {}", referenceNumber, ex);
            return;
        }

        if (result != null && result.ack()) {
            log.info("Safe! Message reached RabbitMQ for: {}", referenceNumber);
        } else {
            String reason = (result != null) ? result.reason() : "Unknown reason";
            log.error("RabbitMQ rejected (Nack-ed) the message for: {}. Reason: {}", referenceNumber, reason);
        }
    }
}