package com.aimr.notify.bootstrap;

import com.aimr.notify.exception.ConnectionTimeoutException;
import com.mongodb.client.MongoClient;
import com.aimr.notify.config.ApplicationProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class ConnectionValidator {

    private final RedisConnectionFactory redisConnectionFactory;

    private final MongoClient mongoClient;
    private final ApplicationProperties properties;

    @Autowired
    private DataSource dataSource;


    @PostConstruct
    public void init() {
        try {
            testRedisConnection();
            testMongoConnection();
            testKafkaConnection();
            testPostgresConnection();
        } catch (Exception e) {
            log.error("[ConnectionValidator] FATAL: Startup connection validation failed. Exiting JVM........", e);
            System.exit(1);
        }
    }

    private void testKafkaConnection() {
        try (AdminClient client = AdminClient.create(Map.of("bootstrap.servers", properties.getBootstrapKafkaServers()))) {
            ListTopicsResult topics = client.listTopics();
            topics.names().get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error while pinging kafka connection : ", e);
            throw new ConnectionTimeoutException("Error while pinging kafka connection",HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private void testMongoConnection() {
        try {
            mongoClient.listDatabases().first();
        } catch (Exception e) {
            log.error("Error while pinging mongo connection");
            throw new ConnectionTimeoutException("Error while pinging mongo connection",HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private void testPostgresConnection() {
        try {
            Connection connection = dataSource.getConnection();
            boolean isValid = connection.isValid(3);
            if (!isValid) {
                throw new SQLException("Error while pinging  postgres connection");
            }
        } catch (SQLException e) {
            String message=e.getMessage();
            log.error(message);
            throw new ConnectionTimeoutException(message,HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private void testRedisConnection() {
        try {
            redisConnectionFactory.getConnection().ping();
        } catch (Exception e) {
            log.error("Error while pinging redis connection");
            throw new ConnectionTimeoutException("Error while pinging redis connection",HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

}
