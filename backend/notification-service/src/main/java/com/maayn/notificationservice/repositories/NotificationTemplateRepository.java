package com.maayn.notificationservice.repositories;

import com.maayn.notificationservice.documents.notification.NotificationTemplateDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationTemplateRepository extends MongoRepository<NotificationTemplateDocument, String> {

    Optional<NotificationTemplateDocument> findByEventTypeAndChannelAndIsActiveTrue(
            String eventType, String channel
    );

    List<NotificationTemplateDocument> findByEventTypeAndChannel(String eventType, String channel);

    List<NotificationTemplateDocument> findByIsActiveTrue();
}