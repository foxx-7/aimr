package com.aimr.notify.models.dto.response;

import com.aimr.notify.models.enums.NotificationChannel;
import com.aimr.notify.models.enums.NotificationStatus;
import com.aimr.notify.models.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponse {

    private String tenantId;
    private String requestId;
    private String traceId;
    private String templateId;
    private NotificationChannel dispatchChannel;
    private NotificationStatus status;
    private Instant createdAt;
    private Instant submittedAt;

    public NotificationResponse(Notification notification) {
        setTenantId(notification.getTenantId());
        setTemplateId(notification.getTemplateId());
        setRequestId(notification.getRequestId());
        setDispatchChannel(notification.getDispatchChannel());
        setStatus(notification.getStatus());
        setCreatedAt(notification.getCreatedAt());
        setSubmittedAt(notification.getSubmittedAt());
    }
}

