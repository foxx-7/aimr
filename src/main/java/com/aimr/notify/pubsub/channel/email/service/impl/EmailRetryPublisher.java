package com.aimr.notify.pubsub.channel.email.service.impl;

import com.aimr.notify.config.ApplicationProperties;
import com.aimr.notify.model.dto.ChannelDispatchDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import org.springframework.messaging.Message;
import static com.aimr.notify.constant.ApplicationConstants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailRetryPublisher {

    private final ApplicationProperties properties;
    private final KafkaTemplate<@NonNull String, @NonNull ChannelDispatchDTO> kafkaTemplate;

    public void publishToRetry(ChannelDispatchDTO dispatch, int retryCount, String reason) {
        if (retryCount > MAX_EMAIL_RETRIES) {
            publishToDlt(dispatch, reason);
            return;
        }

        log.warn("[EmailRetryPublisher] Scheduling retry {}/{} requestId={}",
                retryCount, MAX_EMAIL_RETRIES, dispatch.getRequestId());

        Message<@NonNull ChannelDispatchDTO> message=MessageBuilder
                .withPayload(dispatch)
                .setHeader(KafkaHeaders.TOPIC, properties.getEmailRetryTopic())
                .setHeader(KafkaHeaders.KEY, dispatch.getTenantId())
                .setHeader("RETRY_COUNT", retryCount)
                .build();

        sendToQueue(message, dispatch.getRequestId());
    }

    public void publishToDlt(ChannelDispatchDTO dispatch, String reason) {
        log.error("[EmailRetryPublisher] Routing to DLT requestId={} reason={}",
                dispatch.getRequestId(), reason);

        Message<@NonNull ChannelDispatchDTO> message = MessageBuilder
                .withPayload(dispatch)
                .setHeader(KafkaHeaders.TOPIC, properties.getEmailDltTopic())
                .setHeader("FAILURE_REASON", reason)
                .setHeader("RETRY_COUNT", MAX_EMAIL_RETRIES)
                .setHeader(KafkaHeaders.KEY, dispatch.getTenantId())
                .build();

        sendToQueue(message, dispatch.getRequestId());

    }

    private void sendToQueue(Message<@NonNull ChannelDispatchDTO> message, String requestId){
        kafkaTemplate.send(message)
                .exceptionally(ex -> {
                    log.error("[EmailRetryPublisher] Failed to publish retry for requestId={} — reason: {}",
                            requestId, ex.getMessage());
                    // optionally: mark notification FAILED or alert here
                    return null;
                });
    }
}