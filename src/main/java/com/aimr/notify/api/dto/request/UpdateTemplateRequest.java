package com.aimr.notify.api.dto.request;

import java.util.Map;

public record UpdateTemplateRequest(
    String name,
    Map<String, String> templateVariables,
    String messageTemplate
) {
}