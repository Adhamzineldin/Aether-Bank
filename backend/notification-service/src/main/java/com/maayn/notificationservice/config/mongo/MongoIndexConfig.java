package com.maayn.notificationservice.config.mongo;

import com.maayn.notificationservice.documents.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.bson.Document;

// Run automatically on startup
@Configuration
// Make the class injectable using the private final
@RequiredArgsConstructor
// logger
@Slf4j
public class MongoIndexConfig {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
//    Execute after Spring Initialize the bean
    public void initIndexes() {
        log.info("Initializing MongoDB indexes...");

        // ── workflow_instances ─────────────────────────
        mongoTemplate.indexOps(WorkflowInstanceDocument.class)
                .ensureIndex(new Index()
                        .on("entityId", Sort.Direction.ASC)
                        .named("idx_workflow_entityId"));

        mongoTemplate.indexOps(WorkflowInstanceDocument.class)
                .ensureIndex(new Index()
                        .on("status", Sort.Direction.ASC)
                        .named("idx_workflow_status"));

        mongoTemplate.indexOps(WorkflowInstanceDocument.class)
                .ensureIndex(new Index()
                        .on("entityId", Sort.Direction.ASC)
                        .on("status", Sort.Direction.ASC)
                        .named("idx_workflow_entityId_status"));

        // ── approval_tasks ─────────────────────────────
        mongoTemplate.indexOps(ApprovalTaskDocument.class)
                .ensureIndex(new Index()
                        .on("workflowId", Sort.Direction.ASC)
                        .named("idx_task_workflowId"));

        mongoTemplate.indexOps(ApprovalTaskDocument.class)
                .ensureIndex(new Index()
                        .on("assignedTo", Sort.Direction.ASC)
                        .named("idx_task_assignedTo"));

        mongoTemplate.indexOps(ApprovalTaskDocument.class)
                .ensureIndex(new Index()
                        .on("assignedTo", Sort.Direction.ASC)
                        .on("taskStatus", Sort.Direction.ASC)
                        .named("idx_task_assignedTo_status"));

        // ── notification_items ─────────────────────────
        mongoTemplate.indexOps(NotificationItemDocument.class)
                .ensureIndex(new Index()
                        .on("userId", Sort.Direction.ASC)
                        .named("idx_notification_userId"));

        mongoTemplate.indexOps(NotificationItemDocument.class)
                .ensureIndex(new Index()
                        .on("status", Sort.Direction.ASC)
                        .named("idx_notification_status"));

        mongoTemplate.indexOps(NotificationItemDocument.class)
                .ensureIndex(new Index()
                        .on("correlationId", Sort.Direction.ASC)
                        .sparse()    // many docs won't have this field
                        .named("idx_notification_correlationId"));

        mongoTemplate.indexOps(NotificationItemDocument.class)
                .ensureIndex(new Index()
                        .on("userId", Sort.Direction.ASC)
                        .on("type", Sort.Direction.ASC)
                        .named("idx_notification_userId_type"));

        // ── notification_templates ─────────────────────
        mongoTemplate.indexOps(NotificationTemplateDocument.class)
                .ensureIndex(new Index()
                        .on("eventType", Sort.Direction.ASC)
                        .on("channel", Sort.Direction.ASC)
                        .on("isActive", Sort.Direction.ASC)
                        .unique()     // only one active template per eventType+channel
                        .named("idx_template_eventType_channel_active"));

        log.info("MongoDB indexes initialized successfully.");
    }
}