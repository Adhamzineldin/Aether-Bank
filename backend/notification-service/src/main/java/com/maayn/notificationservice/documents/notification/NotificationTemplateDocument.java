package com.maayn.notificationservice.documents.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;
@Document(collection = "notification_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateDocument {
    @Id
    private UUID id;

    private String eventType;
    private String channel;
    private String titleTemplate;
    private String bodyTemplate;
    private Integer version;
    private Boolean isActive;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

