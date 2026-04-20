package com.maayn.notificationservice.services.transfer;

import com.maayn.notificationservice.dto.TransferFailedPayload;
import com.maayn.notificationservice.dto.transfer.TransferSuccessPayload;
import com.maayn.notificationservice.templates.EmailTemplate;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class TransferEmailNotifier {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String fromAddress;

    public TransferEmailNotifier(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${spring.mail.username:}") String fromAddress
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.fromAddress = fromAddress;
    }

    public void sendTransferSuccess(String toEmail, TransferSuccessPayload event) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn("JavaMailSender not available; skip success email for ref {}", event.referenceNumber());
            return;
        }
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            if (fromAddress != null && !fromAddress.isBlank()) {
                helper.setFrom(fromAddress);
            }
            helper.setTo(toEmail);
            helper.setSubject(EmailTemplate.transferSuccessSubject(event.referenceNumber()));
            helper.setText(EmailTemplate.transferSuccessBody(event), false);
            sender.send(message);
            log.info("Sent transfer success email for ref {} to {}", event.referenceNumber(), toEmail);
        } catch (Exception e) {
            log.error("Failed to send transfer success email for ref {}: {}", event.referenceNumber(), e.getMessage());
        }
    }

    public void sendTransferFailed(String toEmail, TransferFailedPayload event) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn("JavaMailSender not available; skip failure email for ref {}", event.referenceNumber());
            return;
        }
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            if (fromAddress != null && !fromAddress.isBlank()) {
                helper.setFrom(fromAddress);
            }
            helper.setTo(toEmail);
            helper.setSubject(EmailTemplate.transferFailedSubject(event.referenceNumber()));
            helper.setText(EmailTemplate.transferFailedBody(event), false);
            sender.send(message);
            log.info("Sent transfer failed email for ref {} to {}", event.referenceNumber(), toEmail);
        } catch (Exception e) {
            log.error("Failed to send transfer failed email for ref {}: {}", event.referenceNumber(), e.getMessage());
        }
    }
}
