package com.maayn.notificationservice.config.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(
        basePackages = "com.maayn.notificationservice.repositories"
)
public class MongoConfig extends AbstractMongoClientConfiguration {
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Override
    protected String getDatabaseName() {
        return "workflow_notification_service";
    }

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    // Remove _class field from every document
    // By default Spring Data MongoDB writes a "_class" field
    // into every document. This removes it to keep documents clean.
    //  e.g.   "_class": "com.maayn.Notification"

    @Bean
    public MappingMongoConverter mappingMongoConverter(
            MongoDatabaseFactory factory,
            MongoMappingContext context
//            Check how to handle errors using Veld
    ) throws Exception {
        MappingMongoConverter converter =
                new MappingMongoConverter(new org.springframework.data.mongodb.core.convert
                .DefaultDbRefResolver(factory),
                context);
        converter.setTypeMapper(new DefaultMongoTypeMapper(null)); // null = no _class field

        return converter;
    }


    // Transaction support // Will check it later
    // Required if you want multi-document transactions
    // (needs MongoDB replica set or use mongod --replSet)

    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory factory) {
        return new MongoTransactionManager(factory);
    }
}
