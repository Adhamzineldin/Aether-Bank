package com.maayn.notificationservice.config.rabbit;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange bankingExchange() {
        return new TopicExchange(NotificationRabbitConfig.BANKING_EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationTransferEventsQueue() {
        return new Queue(NotificationRabbitConfig.NOTIFICATION_TRANSFER_EVENTS_QUEUE, true);
    }

    @Bean
    public Binding notificationTransferSuccessBinding(
            Queue notificationTransferEventsQueue,
            TopicExchange bankingExchange
    ) {
        return BindingBuilder.bind(notificationTransferEventsQueue)
                .to(bankingExchange)
                .with(NotificationRabbitConfig.TRANSFER_SUCCESS_ROUTING_KEY);
    }

    @Bean
    public Binding notificationTransferFailedBinding(
            Queue notificationTransferEventsQueue,
            TopicExchange bankingExchange
    ) {
        return BindingBuilder.bind(notificationTransferEventsQueue)
                .to(bankingExchange)
                .with(NotificationRabbitConfig.TRANSFER_FAILED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
