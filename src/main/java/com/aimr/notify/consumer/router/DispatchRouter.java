package com.aimr.notify.consumer.router;

import com.aimr.notify.config.ApplicationProperties;
import com.aimr.notify.consumer.dto.ChannelDispatchDTO;
import com.aimr.notify.consumer.dto.IngestTopicDTO;
import com.aimr.notify.domain.enums.NotificationChannel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static com.aimr.notify.constant.ApplicationConstants.X_REQUEST_ID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DispatchRouter {

    private final ApplicationProperties properties;
    private final KafkaTemplate<@NonNull String, @NonNull ChannelDispatchDTO> kafkaTemplate;

    @KafkaListener(
            topics = "notification.ingest",
            groupId = "dispatch-router-group",
            concurrency = "6"
    )
    public void route(IngestTopicDTO ingest, Acknowledgment ack) {
        String requestId = ingest.getRequestId();
        MDC.put(X_REQUEST_ID, requestId);
        log.info("[DispatchRouter] Routing requestId={} tenantId={} type={}",
                requestId, ingest.getTenantId(), ingest.getChannel());

        try{
            // 3. Build the channel dispatch payload
            ChannelDispatchDTO dispatch = ChannelDispatchDTO.builder()
                    .requestId(requestId)
                    .tenantId(ingest.getTenantId())
                    .templateId(ingest.getTemplateId())
                    .recipientBinding(ingest.getRecipientBinding())
                    .senderIdentity(ingest.getSenderIdentity())
                    .dynamicVariables(ingest.getDynamicVariables())
                    .subject(ingest.getSubject())
                    .message(ingest.getMessage())
                    .channel(ingest.getChannel())
                    .receivedAt(ingest.getReceivedAt())
                    .routedAt(Instant.now())
                    .build();

            // 4. Route to the correct topic
            String targetTopic = resolveTopic(ingest.getChannel());

            kafkaTemplate.send(targetTopic, ingest.getTenantId(), dispatch)
                    .whenComplete((_, ex) -> {
                        if (ex != null) {
                            log.error("[DispatchRouter] Publish failed requestId={}. topic={}",
                                    requestId, targetTopic, ex);
                            //Kafka will redeliver
                        } else {
                            log.info("[DispatchRouter] Published requestId={} → {}", requestId, targetTopic);
                            ack.acknowledge();
                        }
                    });

        } catch (Exception e) {
            log.error("[DispatchRouter] Unexpected error requestId={} — will retry", requestId, e);
            // no ack ..Kafka will redeliver
        } finally {
            MDC.clear();//clear MDC
        }
    }

    private String resolveTopic(NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> properties.getEmailTopic();
            case PUSH  -> properties.getPushTopic();
            case SMS   -> properties.getSmsTopic();
            case WEBHOOK -> properties.getWebHookTopic();
        };
    }
}