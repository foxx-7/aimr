package com.aimr.notify.service;

import com.aimr.notify.config.ApplicationProperties;
import com.aimr.notify.exception.DataConversionException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final KafkaTemplate<@NonNull String, @NonNull String> kafkaTemplate;
    private final JsonMapper mapper;
    private final ApplicationProperties properties;

    public boolean sendDataToIngest(final Object input) {
        return sendNotification(properties.getIngestTopic(), convertDataIntoString(input));
    }

    public boolean sendDataToBroadcast(final Object input){
        return sendNotification(properties.getBroadcastTopic(),convertDataIntoString(input));
    }

    public boolean sendDataToAudit(final Object input) {
        return sendNotification(properties.getAuditTopic(), convertDataIntoString(input));
    }

    public boolean sendNotification(String topic, String message) {
        try {
            kafkaTemplate.send(topic, message);
            log.info("Kafka Data Published to Topic : {}", topic);
            return true;
        } catch (Exception e) {
            log.error("Error while publishing data to dto For Topic : {} With Error : ", topic, e);
            return false;
        }
    }

    private String convertDataIntoString(final Object input) {
        try {
            return mapper.writeValueAsString(input);
        } catch (Exception e) {
            throw new DataConversionException("Error while serializing input payload");
        }
    }
}
