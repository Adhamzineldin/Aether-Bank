package com.maayn.notificationservice.repositories;

import com.maayn.notificationservice.documents.notification.NotificationItemDocument;
import maayn.veld.generated.models.NotificationStatus;
import maayn.veld.generated.models.NotificationType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationItemRepository extends MongoRepository<NotificationItemDocument, String> {
    List<NotificationItemDocument> findByUserId(UUID userId);
    List<NotificationItemDocument> findByUserIdAndType(UUID userId, NotificationType type);
    List<NotificationItemDocument> findByStatus(NotificationStatus status);
}
