package com.aimr.notify.pubsub.channel.email.service.impl;

import com.aimr.notify.model.dto.ChannelDispatchDTO;
import com.aimr.notify.model.enums.NotificationStatus;
import com.aimr.notify.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import static com.aimr.notify.constant.ApplicationConstants.X_REQUEST_ID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailDltWorker {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${kafka.email.dlt.topic}",
            groupId = "${kafka.email.dlt.group}"
    )
    public void consume(
            ChannelDispatchDTO dispatch,
            @Header(value = "FAILURE_REASON", required = false) String reason,
            Acknowledgment ack) {

        String safeReason = reason != null ? reason : "unknown";

        String requestId = dispatch.getRequestId();
        MDC.put(X_REQUEST_ID, requestId);

        log.error("[EmailDltWorker] requestId={} permanently failed — reason={}",
                requestId, safeReason);

        try {
            log.error("[EmailDltWorker] requestId={} tenantId={} permanently failed — reason={}",
                    requestId, dispatch.getTenantId(), reason);

            notificationService.markNotificationStatus(dispatch.getTenantId(), requestId,
                    null, NotificationStatus.FAILED);
            ack.acknowledge();
        } finally {
            MDC.clear();
        }
    }
}