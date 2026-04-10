package com.maayn.transactionservice.events;

import com.maayn.transactionservice.entity.Transaction;
import com.maayn.transactionservice.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.shared.TransferInitiatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferSagaPublisher {

    private final RabbitTemplate rabbitTemplate;

}