package com.aimr.notify.models.dto;

import com.aimr.notify.models.enums.NotificationChannel;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Builder
//@Jacksonized
@Data
public class ChannelDispatchDTO {
    private String requestId;
    private String tenantId;
    private String templateId;
    private Map<String, String> dynamicVariables;
    private String subject;
    private String message;
    private NotificationChannel channel;
    private Instant receivedAt;
    private Instant routedAt;
}