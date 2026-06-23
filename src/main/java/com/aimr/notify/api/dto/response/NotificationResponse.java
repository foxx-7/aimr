package com.aimr.notify.api.dto.response;

import com.aimr.notify.domain.enums.NotificationChannel;
import com.aimr.notify.domain.enums.NotificationStatus;
import com.aimr.notify.domain.entity.Notification;
import java.time.Instant;

import lombok.Builder;
@Builder
public record NotificationResponse(
    String tenantId,
    String requestId,
    String traceId,
    String templateId,
    NotificationChannel dispatchChannel,
    NotificationStatus status,
    Instant createdAt,
    Instant submittedAt
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
            notification.getTenantId(),
            notification.getRequestId(),
            notification.getTraceId(),
            notification.getTemplateId(),
            notification.getDispatchChannel(),
            notification.getStatus(),
            notification.getCreatedAt(),
            notification.getCreatedAt()
        );
    }
}