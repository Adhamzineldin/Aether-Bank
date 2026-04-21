package com.maayn.notificationservice.mappers;

import com.maayn.notificationservice.documents.notification.NotificationItemDocument;
import com.maayn.notificationservice.documents.notification.NotificationTemplateDocument;
import maayn.veld.generated.models.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Component
public class NotificationMapper {

    public NotificationItem toModel(NotificationItemDocument doc) {
        if (doc == null) {
            return null;
        }
        return new NotificationItem(
                doc.getId() != null ? UUID.fromString(doc.getId()) : null,
                doc.getUserId(),
                doc.getType(),
                doc.getChannel(),
                doc.getEventType(),
                doc.getCorrelationId(),
                doc.getTemplateId(),
                doc.getTemplateVersion(),
                doc.getTitle(),
                doc.getMessage(),
                doc.getStatus(),
                doc.getRetryCount(),
                doc.getProcessedAt(),
                doc.getSentAt(),
                doc.getFailedReason(),
                doc.getCreatedAt()
        );
    }

    public NotificationTemplate templateToModel(NotificationTemplateDocument doc) {
        if (doc == null) {
            return null;
        }
        return new NotificationTemplate(
                doc.getId() != null ? UUID.fromString(doc.getId()) : null,
                doc.getEventType() != null ? NotificationEventType.fromValue(doc.getEventType()) : null,
                doc.getChannel() != null ? NotificationChannel.fromValue(doc.getChannel()) : null,
                doc.getTitleTemplate(),
                doc.getBodyTemplate(),
                doc.getVersion(),
                doc.isActive(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }

    public NotificationTemplateDocument toTemplateDocument(CreateTemplateInput input, long version) {
        return NotificationTemplateDocument.builder()
                .id(UUID.randomUUID().toString())
                .eventType(input.getEventType() != null ? input.getEventType().getValue() : null)
                .channel(input.getChannel() != null ? input.getChannel().getValue() : null)
                .titleTemplate(input.getTitleTemplate())
                .bodyTemplate(input.getBodyTemplate())
                .version(version)
                .isActive(true)
                .build();
    }

    public NotificationItemDocument toItemDocumentForSend(
            SendNotificationInput input,
            String title,
            String message,
            UUID templateId,
            Long templateVersion
    ) {
        return NotificationItemDocument.builder()
                .id(UUID.randomUUID().toString())
                .userId(input.getUserId())
                .type(input.getType())
                .channel(input.getChannel())
                .eventType(input.getEventType())
                .correlationId(input.getCorrelationId())
                .templateId(templateId)
                .templateVersion(templateVersion)
                .title(title)
                .message(message)
                .status(NotificationStatus.PENDING)
                .retryCount(0L)
                .build();
    }

    public String applyPlaceholders(String template, SendNotificationInput input) {
        if (!StringUtils.hasText(template) || input == null) {
            return template != null ? template : "";
        }
        String out = template;
        if (input.getUserId() != null) {
            out = out.replace("{userId}", input.getUserId().toString());
        }
        if (input.getEventType() != null) {
            out = out.replace("{eventType}", input.getEventType().getValue());
        }
        if (input.getChannel() != null) {
            out = out.replace("{channel}", input.getChannel().getValue());
        }
        if (input.getType() != null) {
            out = out.replace("{type}", input.getType().getValue());
        }
        if (input.getCorrelationId() != null) {
            out = out.replace("{correlationId}", input.getCorrelationId().toString());
        }
        return out;
    }
}
