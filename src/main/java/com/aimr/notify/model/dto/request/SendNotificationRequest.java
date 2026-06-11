package com.aimr.notify.model.dto.request;

import com.aimr.notify.model.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class SendNotificationRequest {

    @NotBlank(message = "template id is required")
    private String templateId;

    @NotEmpty(message = "dynamic variables must be provided")
    private Map<String, String> dynamicVariables;

    @NotNull(message = "notification channel must be specified")
    private NotificationChannel dispatchChannel;
}
