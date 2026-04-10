package com.maayn.notificationservice.config.mongo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import java.util.Optional;

@Configuration
public class MongoAuditingConfig {

    // Provides the "current user" for @CreatedBy / @LastModifiedBy fields
    // Replace with actual security context lookup when auth is wired in

//    Bean means spring will control it
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("system");
    }
}