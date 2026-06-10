package com.aimr.notify.pubsub.queue.publisher.impl;

import com.aimr.notify.config.ApplicationProperties;
import com.aimr.notify.exception.DataConversionException;
import com.aimr.notify.pubsub.queue.provider.interfaces.GenericProvider;
import com.aimr.notify.pubsub.queue.publisher.interfaces.GenericFallbackPublisher;
import com.aimr.notify.pubsub.queue.publisher.interfaces.GenericPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
class GenericPublisherImpl implements GenericPublisher {

    private final List<GenericProvider> genericPublishers;
    private final List<GenericFallbackPublisher> genericFallbackPublishers;
    private final JsonMapper mapper;
    private final ApplicationProperties properties;

    @Override
    public boolean sendDataToIngest(final Object input) {
        return sendNotification(properties.getIngestTopic(), convertDataIntoString(input));
    }

    @Override
    public boolean sendDataToAudit(final Object input) {
        return sendNotification(properties.getAuditTopic(), convertDataIntoString(input));
    }


    @Override
    public boolean sendNotification(final String topic, final String message) {
        log.info("sending notification using generic publisher");

        AtomicBoolean success = new AtomicBoolean(false);

        genericPublishers.forEach(publisher -> {
            boolean publisherStatus = publisher.sendNotification(topic, message);
            if (!success.get()) {
                success.set(publisherStatus);
            }
            if (publisherStatus) {
                log.info("notification send to topic : {} using provider : {}",
                        topic, publisher.getClass().getSimpleName());
            } else {
                log.error("Error while publishing data for topic : {} using provider :{}",
                        topic, publisher.getClass().getSimpleName());
            }
        });

        genericFallbackPublishers.forEach(fallback -> {
            if (!success.get()) {
                boolean publisherStatus = fallback.sendNotification(topic, message);
                if (publisherStatus) {
                    success.set(true);
                    log.info("notification send to topic : {} using fallback provider : {}",
                            topic, fallback.getClass().getSimpleName());
                } else {
                    // persist in a fallback repository for later  investigation and manual handling  by admin
                    log.error("Error while publishing data for topic : {} using fallback provider :{}",
                            topic, fallback.getClass().getSimpleName());
                }
            }
        });
        return success.get();
    }

    private String convertDataIntoString(final Object input) {
        try {
            return mapper.writeValueAsString(input);
        } catch (Exception e) {
            throw new DataConversionException("Error while parsing input payload");
        }
    }


}
