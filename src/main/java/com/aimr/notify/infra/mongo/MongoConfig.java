package com.aimr.notify.infra.mongo;

import com.aimr.notify.config.ApplicationProperties;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class MongoConfig {

    private final ApplicationProperties properties;

    @Bean
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(properties.getMongoConnectionURI());

        MongoClientSettings build = MongoClientSettings
                .builder()
                .applyConnectionString(connectionString)
                .applyToSocketSettings(builder -> builder.connectTimeout(5, TimeUnit.SECONDS).readTimeout(5, TimeUnit.SECONDS))
                .applyToClusterSettings(builder -> builder.serverSelectionTimeout(5, TimeUnit.SECONDS))
                .build();

        return MongoClients.create(build);
    }

}
