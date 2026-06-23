package com.aimr.notify.consumer.channel.email.function;

import com.aimr.notify.consumer.dto.ChannelDispatchDTO;
import com.aimr.notify.domain.enums.NotificationStatus;
import com.aimr.notify.service.NotificationService;
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

        String safeReason = reason != null ? reason : "Maximum retry threshold exceeded or payload format corrupt";
        String requestId = dispatch.getRequestId();
        
        MDC.put(X_REQUEST_ID, requestId);

        try {
            // FIX: Added missing {} token to prevent safeReason parameter from getting dropped in output logs
            log.error("[EmailDltWorker] Critical: Notification permanently failed. RequestId={}, TenantId={}, Reason={}",
                    requestId, dispatch.getTenantId(), safeReason);

            notificationService.markNotificationStatus(
                    dispatch.getTenantId(), 
                    requestId,
                    null, 
                    NotificationStatus.FAILED
            );
            
            ack.acknowledge();
        } finally {
            MDC.clear();
        }
    }
}