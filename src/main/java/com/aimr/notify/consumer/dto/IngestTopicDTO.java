package com.aimr.notify.consumer.dto;

import com.aimr.notify.domain.enums.NotificationChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IngestTopicDTO {
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

}
