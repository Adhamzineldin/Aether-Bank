package com.maayn.notificationservice.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventListener {

    private final JavaMailSender mailSender;

    @RabbitListener(queues = "transaction.success.queue")
    public void onTransferSuccess(Map<String, Object> event) {
        try {
            log.info("Received transaction success event: {}", event);

            String transactionId = event.get("transactionId").toString();
            String amount = event.get("amount").toString();
            String currency = event.get("currency").toString();

            // TODO: Fetch customer email from Account Service
            // sendEmail(customerEmail, "Transfer Successful", 
            //          "Your transfer of " + amount + " " + currency + " was successful.");

            log.info("Transaction notification sent for: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to process transaction success event", e);
        }
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("noreply@aetherbank.com");

            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }
}

