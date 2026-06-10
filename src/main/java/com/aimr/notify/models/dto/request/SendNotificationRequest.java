package com.aimr.notify.models.dto.request;

import com.aimr.notify.models.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

import static com.aimr.notify.constants.ErrorConstants.*;

@Data
public class SendNotificationRequest {

    @NotBlank(message = "template id is required")
    private String templateId;

    @NotEmpty(message = "dynamic variables must be provided")
    private Map<String, String> dynamicVariables;

    @NotNull(message = "notification channel must be specified")
    private NotificationChannel dispatchChannel;
}
