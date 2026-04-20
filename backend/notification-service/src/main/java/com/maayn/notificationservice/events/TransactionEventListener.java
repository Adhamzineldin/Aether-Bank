package com.maayn.notificationservice.events;

import com.maayn.notificationservice.templates.EmailTemplateEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.sdk.account.AccountClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventListener {

    private final JavaMailSender mailSender;
    private final EmailTemplateEngine templateEngine;
    private final AccountClient accountClient;

    @RabbitListener(queues = "transaction.success.queue")
    public void onTransferSuccess(Map<String, Object> event) {
        try {
            log.info("Received transaction success event: {}", event);

            String referenceNumber = event.get("referenceNumber").toString();
            BigDecimal amount = new BigDecimal(event.get("amount").toString());
            String currency = event.get("currency").toString();
            UUID sourceAccountId = UUID.fromString(event.get("sourceAccountId").toString());
            UUID destinationAccountId = UUID.fromString(event.get("destinationAccountId").toString());
            LocalDateTime timestamp = LocalDateTime.parse(event.get("timestamp").toString());

            // Fetch account details
            String sourceAccountNumber = getAccountNumber(sourceAccountId);
            String destAccountNumber = getAccountNumber(destinationAccountId);

            // TODO: Fetch customer email from Account Service
            String customerEmail = "customer@example.com"; // Placeholder

            // Send email
            String htmlContent = templateEngine.transferSuccessEmail(
                referenceNumber, amount, currency,
                sourceAccountNumber, destAccountNumber, timestamp
            );

            sendHtmlEmail(customerEmail, "Transfer Successful - Aether Bank", htmlContent);
            log.info("Transfer notification email sent for transaction: {}", referenceNumber);

        } catch (Exception e) {
            log.error("Failed to process transaction success event", e);
        }
    }

    private String getAccountNumber(UUID accountId) {
        try {
            var account = accountClient.account.getAccount(accountId.toString());
            return account.getAccount().getAccountNumber();
        } catch (Exception e) {
            log.warn("Failed to fetch account number for {}", accountId);
            return accountId.toString().substring(0, 8);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("noreply@aetherbank.com");

            mailSender.send(message);
            log.info("HTML email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }
}

