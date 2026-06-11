package com.aimr.notify.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

@Data
public class UpdateTemplateRequest {

    //supported for post updates only
    @NotBlank(message = "name field is required")
    private String name;
    @NotEmpty(message = "template variables must be provided")
    private Map<String, String> templateVariables;
    @NotBlank(message = "messageTemplate field is required")
    private String messageTemplate;

}
