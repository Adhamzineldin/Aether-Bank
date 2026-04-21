package com.maayn.notificationservice.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maayn.notificationservice.config.rabbit.NotificationRabbitConfig;
import com.maayn.notificationservice.dto.TransferFailedPayload;
import com.maayn.notificationservice.dto.TransferSuccessPayload;
import com.maayn.notificationservice.services.transfer.TransferAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferEventListener {

    private final ObjectMapper objectMapper;
    private final TransferAlertService transferAlertService;

    @RabbitListener(queues = NotificationRabbitConfig.NOTIFICATION_TRANSFER_EVENTS_QUEUE)
    public void onTransferEvent(
            String body,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey
    ) throws Exception {
        log.debug("Transfer event rk={} payload={}", routingKey, body);

        if (NotificationRabbitConfig.TRANSFER_SUCCESS_ROUTING_KEY.equals(routingKey)) {
            transferAlertService.handleSuccess(objectMapper.readValue(body, TransferSuccessPayload.class));
        } else if (NotificationRabbitConfig.TRANSFER_FAILED_ROUTING_KEY.equals(routingKey)) {
            transferAlertService.handleFailure(objectMapper.readValue(body, TransferFailedPayload.class));
        } else {
            log.warn("Ignoring message with unexpected routing key: {}", routingKey);
        }
    }
}
