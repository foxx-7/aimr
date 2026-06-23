package com.aimr.notify.consumer.channel.email.function;

import com.aimr.notify.config.ApplicationProperties;
import com.aimr.notify.consumer.dto.ChannelDispatchDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.messaging.Message;

import static com.aimr.notify.constant.ApplicationConstants.MAX_EMAIL_RETRIES;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailRetryPublisher {

    private final ApplicationProperties properties;
    private final KafkaTemplate<@NonNull String, @NonNull ChannelDispatchDTO> kafkaTemplate;

    public void publishToRetry(ChannelDispatchDTO dispatch, int retryCount, String reason) {
        // TODO: Query the specific tenant's limit configurations at runtime
        int maxTenantRetries = MAX_EMAIL_RETRIES;

        if (retryCount > maxTenantRetries) {
            publishToDlt(dispatch, reason, maxTenantRetries);
            return;
        }

        log.warn("[EmailRetryPublisher] Scheduling retry attempt {}/{} for tenant={} requestId={}",
                retryCount, maxTenantRetries, dispatch.getTenantId(), dispatch.getRequestId());

        Message<@NonNull ChannelDispatchDTO> message = MessageBuilder
                .withPayload(dispatch)
                .setHeader(KafkaHeaders.TOPIC, properties.getEmailRetryTopic())
                .setHeader(KafkaHeaders.KEY, dispatch.getTenantId()) // Routes cleanly to tenant partition targets
                .setHeader("RETRY_COUNT", retryCount)
                .build();

        sendToQueue(message, dispatch.getRequestId());
    }

    public void publishToDlt(ChannelDispatchDTO dispatch, String reason, int maxRetries) {
        log.error("[EmailRetryPublisher] Maximum lifecycle limits hit. Routing to DLT requestId={} reason={}",
                dispatch.getRequestId(), reason);

        Message<@NonNull ChannelDispatchDTO> message = MessageBuilder
                .withPayload(dispatch)
                .setHeader(KafkaHeaders.TOPIC, properties.getEmailDltTopic())
                .setHeader("FAILURE_REASON", reason)
                .setHeader("RETRY_COUNT", maxRetries)
                .setHeader(KafkaHeaders.KEY, dispatch.getTenantId())
                .build();

        sendToQueue(message, dispatch.getRequestId());
    }

    private void sendToQueue(Message<@NonNull ChannelDispatchDTO> message, String requestId) {
        kafkaTemplate.send(message)
                .exceptionally(ex -> {
                    log.error("[EmailRetryPublisher] Infrastructure failed publishing to topic for requestId={} | reason: {}",
                            requestId, ex.getMessage());
                    return null;
                });
    }
}