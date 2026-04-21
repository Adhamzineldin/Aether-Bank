package com.maayn.notificationservice.config.mongo;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.maayn.notificationservice.repositories")
public class MongoConfig extends AbstractMongoClientConfiguration {
    // Support both the new Spring Boot 4 prefix (spring.mongodb.uri) and the legacy
    // (spring.data.mongodb.uri) prefix as a fallback so the URI can come from either
    // the SPRING_MONGODB_URI or SPRING_DATA_MONGODB_URI environment variable.
    @Value("${spring.mongodb.uri:${spring.data.mongodb.uri:mongodb://localhost:27017/notification_db}}")
    private String mongoUri;
    @Override
    protected String getDatabaseName() {
        return "workflow_notification_service";
    }
    @Override
    public MongoClient mongoClient() {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoUri))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build();
        return MongoClients.create(settings);
    }
    @Bean
    public MappingMongoConverter mappingMongoConverter(
            MongoDatabaseFactory factory,
            MongoMappingContext context
    ) {
        MappingMongoConverter converter =
                new MappingMongoConverter(new DefaultDbRefResolver(factory), context);
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return converter;
    }
    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory factory) {
        return new MongoTransactionManager(factory);
    }
}