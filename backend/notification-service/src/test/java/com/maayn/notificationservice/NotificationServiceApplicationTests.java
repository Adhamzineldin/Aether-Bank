package com.maayn.notificationservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.data.mongodb.uri=mongodb://localhost:27017/notification_test",
    "spring.mail.username=test@example.com",
    "spring.mail.password=test-password",
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false",
    "spring.rabbitmq.listener.simple.auto-startup=false",
    "spring.rabbitmq.listener.direct.auto-startup=false"
})
class NotificationServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
