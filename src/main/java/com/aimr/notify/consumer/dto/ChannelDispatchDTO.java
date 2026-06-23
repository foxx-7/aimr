package com.aimr.notify.consumer.dto;

import com.aimr.notify.domain.enums.NotificationChannel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelDispatchDTO {
    private String requestId;
    private String tenantId;
    private String templateId;
    private String recipientBinding;
    private String senderIdentity;
    private Map<String, String> dynamicVariables;
    private String subject;
    private String message;
    private NotificationChannel channel;
    private Instant receivedAt;
    private Instant routedAt;
}