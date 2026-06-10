package com.aimr.notify.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ApplicationProperties {
    @Value("${kafka.audit.topic}")
    private String auditTopic;

    @Value("${kafka.ingest.topic}")
    private String ingestTopic;

    @Value("${kafka.email.topic}")
    private String emailTopic;

    @Value("${kafka.email.retry.topic}")
    private String emailRetryTopic;

    @Value("${kafka.email.dlt.topic}")
    private String emailDltTopic;

    @Value("${kafka.email.retry.group}")
    private String emailRetryGroup;

    @Value("${kafka.email.dlt.group}")
    private String emailDltGroup;

    @Value("${kafka.push.topic}")
    private String pushTopic;

    @Value("${kafka.sms.topic}")
    private String smsTopic;

    @Value("${kafka.webhook.topic}")
    private String WebHookTopic;


    @Value("${spring.mongodb.uri}")
    private String mongoConnectionURI;

    @Value("${spring.datasource.url}")
    private String postgresqlConnectionURI;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapKafkaServers;

    @Value("${sendgrid.api-key}")
    private String sendgridApikey;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;
}