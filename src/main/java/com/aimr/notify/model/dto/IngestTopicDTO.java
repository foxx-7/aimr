package com.aimr.notify.model.dto;

import com.aimr.notify.model.enums.NotificationChannel;
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
    private Map<String, String> dynamicVariables;
    private String subject;
    private String message;
    private NotificationChannel channel;
    private Instant receivedAt;

}
