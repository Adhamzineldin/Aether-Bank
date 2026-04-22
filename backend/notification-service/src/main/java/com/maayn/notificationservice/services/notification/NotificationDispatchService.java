package com.maayn.notificationservice.services.notification;

import com.maayn.notificationservice.audit.AuditPublisher;
import com.maayn.notificationservice.documents.notification.NotificationItemDocument;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.notification.NotificationChannel;
import maayn.veld.generated.models.notification.NotificationStatus;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class NotificationDispatchService {

    private static final int MAX_RETRY = 3;

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final RestClient restClient;
    private final AuditPublisher auditPublisher;

    @Value("${notification.user-contact.url-template:}")
    private String userContactUrlTemplate;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${notification.email.fallback-to:}")
    private String fallbackEmail;

    public NotificationDispatchService(ObjectProvider<JavaMailSender> mailSenderProvider, AuditPublisher auditPublisher) {
        this.mailSenderProvider = mailSenderProvider;
        this.auditPublisher = auditPublisher;
        this.restClient = RestClient.builder().build();
    }

    public void dispatch(NotificationItemDocument doc) {
        LocalDateTime now = LocalDateTime.now();
        if (doc.getChannel() == null) {
            fail(doc, now, "Missing channel");
            return;
        }
        if (doc.getChannel() == NotificationChannel.EMAIL) {
            dispatchEmail(doc, now);
            return;
        }
        doc.setStatus(NotificationStatus.SENT);
        doc.setProcessedAt(now);
        doc.setSentAt(now);
        doc.setFailedReason(null);
        log.info("Recorded non-email notification {} as SENT (channel adapter not wired)", doc.getId());
    }

    private void dispatchEmail(NotificationItemDocument doc, LocalDateTime now) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            fail(doc, now, "JavaMailSender not configured");
            return;
        }
        Optional<String> to = resolveRecipientEmail(doc.getUserId());
        if (to.isEmpty()) {
            fail(doc, now, "No email recipient (configure notification.user-contact.url-template or notification.email.fallback-to)");
            return;
        }
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            if (StringUtils.hasText(mailFrom)) {
                helper.setFrom(mailFrom);
            }
            helper.setTo(to.get());
            helper.setSubject(doc.getTitle() != null ? doc.getTitle() : "Notification");
            // Wrap the plain message in the same branded card layout the
            // transfer alerts use so every email out of notification-service
            // looks like it came from the same product, not a unix mailx
            // dump.
            String htmlBody = com.maayn.notificationservice.templates.GenericEmailTemplate.wrap(
                    doc.getTitle() != null ? doc.getTitle() : "Notification",
                    doc.getMessage() != null ? doc.getMessage() : "");
            helper.setText(htmlBody, true);
            sender.send(message);
            doc.setStatus(NotificationStatus.SENT);
            doc.setProcessedAt(now);
            doc.setSentAt(now);
            doc.setFailedReason(null);
            log.info("Sent email notification {}", doc.getId());
            auditPublisher.publishSuccess("NOTIFICATION_EMAIL_SENT", doc.getUserId(),
                    "id=" + doc.getId() + " to=" + to.get() + " subject=" + (doc.getTitle() != null ? doc.getTitle() : ""));
        } catch (Exception e) {
            log.error("Email send failed for {}: {}", doc.getId(), e.getMessage());
            fail(doc, now, e.getMessage() != null ? e.getMessage() : "Email send failed");
        }
    }

    private void fail(NotificationItemDocument doc, LocalDateTime now, String reason) {
        doc.setStatus(NotificationStatus.FAILED);
        doc.setProcessedAt(now);
        doc.setSentAt(null);
        doc.setFailedReason(reason);
        auditPublisher.publishFailure("NOTIFICATION_DISPATCH_FAILED", doc.getUserId(),
                "id=" + doc.getId() + " channel=" + doc.getChannel() + " reason=" + reason);
    }

    private Optional<String> resolveRecipientEmail(UUID userId) {
        if (userId != null && StringUtils.hasText(userContactUrlTemplate)) {
            String url = UriComponentsBuilder
                    .fromUriString(userContactUrlTemplate.replace("{userId}", userId.toString()))
                    .build(true)
                    .toUriString();
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = restClient.get().uri(url).retrieve().body(Map.class);
                if (body != null) {
                    Object raw = body.get("email");
                    if (raw instanceof String s && StringUtils.hasText(s)) {
                        return Optional.of(s.trim());
                    }
                }
            } catch (Exception e) {
                log.warn("User contact lookup failed for {}: {}", userId, e.getMessage());
            }
        }
        if (StringUtils.hasText(fallbackEmail)) {
            return Optional.of(fallbackEmail.trim());
        }
        return Optional.empty();
    }

    public static boolean mayRetry(NotificationItemDocument doc) {
        long retries = doc.getRetryCount() != null ? doc.getRetryCount() : 0L;
        return retries < MAX_RETRY;
    }

    public static int maxRetry() {
        return MAX_RETRY;
    }
}
