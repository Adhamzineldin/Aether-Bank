package com.maayn.notificationservice.documents.notification;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import maayn.veld.generated.models.notification.NotificationChannel;
import maayn.veld.generated.models.notification.NotificationEventType;
import maayn.veld.generated.models.notification.NotificationStatus;
import maayn.veld.generated.models.notification.NotificationType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "notification_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationItemDocument {

    @Id
    private UUID id;

    private UUID userId;
    private NotificationType type;
    private NotificationChannel channel;
    private NotificationEventType eventType;
    private UUID correlationId;
    private UUID templateId;
    private Integer templateVersion;
    private String title;
    private String message;
    private NotificationStatus status;
    private Integer retryCount;
    private LocalDateTime processedAt;
    private LocalDateTime sentAt;
    private String failedReason;

    @CreatedDate
    private LocalDateTime createdAt;
}