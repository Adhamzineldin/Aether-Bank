package com.maayn.notificationservice.services.notification;

import com.maayn.notificationservice.documents.notification.NotificationItemDocument;
import com.maayn.notificationservice.documents.notification.NotificationTemplateDocument;
import com.maayn.notificationservice.mappers.NotificationMapper;
import com.maayn.notificationservice.repositories.NotificationItemRepository;
import com.maayn.notificationservice.repositories.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.errors.NotificationErrors;
import maayn.veld.generated.models.notification.*;
import maayn.veld.generated.services.INotificationService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final NotificationItemRepository itemRepository;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationDispatchService dispatchService;

    @Override
    public List<NotificationItem> listNotifications() throws Exception {
        return itemRepository.findAll().stream()
                .sorted(Comparator.comparing(NotificationItemDocument::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(notificationMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationItem getNotification(String id) throws Exception {
        String key = requireUuid(id);
        return itemRepository.findById(key)
                .map(notificationMapper::toModel)
                .orElseThrow(() -> NotificationErrors.getNotification.notFound("Notification not found"));
    }

    @Override
    public NotificationItem sendNotification(SendNotificationInput input) throws Exception {
        if (input == null || input.getUserId() == null || input.getEventType() == null
                || input.getChannel() == null || input.getType() == null) {
            throw NotificationErrors.sendNotification.badRequest("userId, eventType, channel, and type are required");
        }

        boolean hasTitle = StringUtils.hasText(input.getTitle());
        boolean hasMessage = StringUtils.hasText(input.getMessage());

        Optional<NotificationTemplateDocument> templateOpt = templateRepository.findByEventTypeAndChannelAndIsActiveTrue(
                input.getEventType().getValue(),
                input.getChannel().getValue()
        );

        UUID templateId = null;
        Integer templateVersion = null;
        String title;
        String message;

        if (!hasTitle || !hasMessage) {
            NotificationTemplateDocument template = templateOpt.orElseThrow(() ->
                    NotificationErrors.sendNotification.notFound(
                            "No active notification template for eventType and channel"));
            templateId = template.getId();
            templateVersion = template.getVersion();
            title = hasTitle
                    ? input.getTitle()
                    : notificationMapper.applyPlaceholders(template.getTitleTemplate(), input);
            message = hasMessage
                    ? input.getMessage()
                    : notificationMapper.applyPlaceholders(template.getBodyTemplate(), input);
        } else {
            if (templateOpt.isPresent()) {
                NotificationTemplateDocument t = templateOpt.get();
                templateId = t.getId();
                templateVersion = t.getVersion();
            }
            title = input.getTitle();
            message = input.getMessage();
        }

        if (!StringUtils.hasText(title) || !StringUtils.hasText(message)) {
            throw NotificationErrors.sendNotification.badRequest("Resolved title and message must not be blank");
        }

        NotificationItemDocument doc = notificationMapper.toItemDocumentForSend(
                input, title, message, templateId, templateVersion);
        doc = itemRepository.save(doc);
        dispatchService.dispatch(doc);
        return notificationMapper.toModel(itemRepository.save(doc));
    }

    @Override
    public NotificationItem retryNotification(String id) throws Exception {
        String key = requireUuid(id);
        NotificationItemDocument doc = itemRepository.findById(key)
                .orElseThrow(() -> NotificationErrors.retryNotification.notFound("Notification not found"));

        if (!NotificationDispatchService.mayRetry(doc)) {
            throw NotificationErrors.retryNotification.badRequest(
                    "Maximum retries (" + NotificationDispatchService.maxRetry() + ") reached");
        }

        if (doc.getStatus() != NotificationStatus.FAILED) {
            throw NotificationErrors.retryNotification.badRequest("Only FAILED notifications can be retried");
        }

        int next = (doc.getRetryCount() != null ? doc.getRetryCount() : 0) + 1;
        doc.setRetryCount(next);
        doc.setStatus(NotificationStatus.PENDING);
        doc.setFailedReason(null);
        doc.setProcessedAt(null);
        doc.setSentAt(null);
        doc = itemRepository.save(doc);
        dispatchService.dispatch(doc);
        return notificationMapper.toModel(itemRepository.save(doc));
    }

    @Override
    public List<NotificationTemplate> listTemplates() throws Exception {
        return templateRepository.findAll().stream()
                .map(notificationMapper::templateToModel)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationTemplate getTemplate(String id) throws Exception {
        String key = requireUuid(id);
        return templateRepository.findById(key)
                .map(notificationMapper::templateToModel)
                .orElseThrow(() -> NotificationErrors.getTemplate.notFound("Template not found"));
    }

    @Override
    public NotificationTemplate createTemplate(CreateTemplateInput input) throws Exception {
        if (input == null || input.getEventType() == null || input.getChannel() == null) {
            throw NotificationErrors.createTemplate.badRequest("eventType and channel are required");
        }
        if (!StringUtils.hasText(input.getTitleTemplate()) || !StringUtils.hasText(input.getBodyTemplate())) {
            throw NotificationErrors.createTemplate.badRequest("titleTemplate and bodyTemplate are required");
        }

        int nextVersion = templateRepository.findByEventTypeAndChannel(
                input.getEventType().getValue(),
                input.getChannel().getValue()
        ).stream()
            .mapToInt(t -> t.getVersion() != null ? t.getVersion() : 0)
                .max()
            .orElse(0) + 1;

        for (NotificationTemplateDocument existing : templateRepository.findByEventTypeAndChannel(
                input.getEventType().getValue(),
                input.getChannel().getValue()
        )) {
            if (Boolean.TRUE.equals(existing.getIsActive())) {
                existing.setIsActive(false);
                templateRepository.save(existing);
            }
        }

        NotificationTemplateDocument saved = templateRepository.save(
                notificationMapper.toTemplateDocument(input, nextVersion));
        return notificationMapper.templateToModel(saved);
    }

    @Override
    public NotificationTemplate updateTemplate(String id, UpdateTemplateInput input) throws Exception {
        String key = requireUuid(id);
        NotificationTemplateDocument doc = templateRepository.findById(key)
                .orElseThrow(() -> NotificationErrors.updateTemplate.notFound("Template not found"));

        boolean touched = false;
        if (input != null && StringUtils.hasText(input.getTitleTemplate())) {
            doc.setTitleTemplate(input.getTitleTemplate());
            touched = true;
        }
        if (input != null && StringUtils.hasText(input.getBodyTemplate())) {
            doc.setBodyTemplate(input.getBodyTemplate());
            touched = true;
        }
        if (!touched) {
            throw NotificationErrors.updateTemplate.badRequest("Provide titleTemplate and/or bodyTemplate");
        }

        int v = doc.getVersion() != null ? doc.getVersion() : 0;
        doc.setVersion(v + 1);

        return notificationMapper.templateToModel(templateRepository.save(doc));
    }

    @Override
    public NotificationTemplate deactivateTemplate(String id) throws Exception {
        String key = requireUuid(id);
        NotificationTemplateDocument doc = templateRepository.findById(key)
                .orElseThrow(() -> NotificationErrors.deactivateTemplate.notFound("Template not found"));
        doc.setIsActive(false);
        return notificationMapper.templateToModel(templateRepository.save(doc));
    }

    private static String requireUuid(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw NotificationErrors.getNotification.notFound("Invalid id");
        }
        try {
            UUID.fromString(raw.trim());
            return raw.trim();
        } catch (IllegalArgumentException e) {
            throw NotificationErrors.getNotification.notFound("Invalid id format");
        }
    }
}
