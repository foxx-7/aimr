package com.aimr.notify.consumer.dto;

import com.aimr.notify.domain.enums.NotificationChannel;
import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class BroadcastTopicDTO {
    private String batchId;
    private String tenantId;
    private NotificationChannel channel;
    private String templateId;
    private String senderIdentity;
    private Map<String, String> dynamicVariables;
    private String subject;
    private String message;
    private String target;
    private String segmentId;
}
