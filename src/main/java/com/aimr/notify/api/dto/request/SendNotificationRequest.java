package com.aimr.notify.api.dto.request;

import com.aimr.notify.domain.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record SendNotificationRequest(
    @NotBlank(message = "template id is required") String templateId,
    @NotEmpty(message = "recipient binding is required") String recipientBinding,
    @NotEmpty(message = "sender identity is required") String senderIdentityId,
    @NotEmpty(message = "dynamic variables must be provided") Map<String, String> dynamicVariables,
    @NotNull(message = "notification channel must be specified") NotificationChannel dispatchChannel
) {
}