package com.aimr.notify.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Map;

import static com.aimr.notify.constant.ErrorConstants.MESSAGE_VARIABLE_ERROR;
import static com.aimr.notify.constant.ErrorConstants.NAME_VARIABLE_ERROR;

public record CreateTemplateRequest(
    @Size(max = 10000, message = NAME_VARIABLE_ERROR)     @NotBlank(message = "Name Field Is Required") String name,
    @NotEmpty(message = "Template Variables Are Required") Map<String, String> templateVariables,
    @Size(max = 1000000, message = MESSAGE_VARIABLE_ERROR)     @NotBlank(message = "Message Template Field Is Required") String messageTemplate
) {
}