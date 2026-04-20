package com.maayn.notificationservice.config.rabbit;

public final class NotificationRabbitConfig {

    private NotificationRabbitConfig() {
    }

    public static final String BANKING_EXCHANGE = "banking.exchange";
    public static final String TRANSFER_SUCCESS_ROUTING_KEY = "transaction.transfer.success";
    public static final String TRANSFER_FAILED_ROUTING_KEY = "transaction.transfer.failed";


    public static final String NOTIFICATION_TRANSFER_EVENTS_QUEUE = "notification.transfer.events.queue";
}
