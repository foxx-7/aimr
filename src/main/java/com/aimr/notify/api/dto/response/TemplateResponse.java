package com.aimr.notify.api.dto.response;

import com.aimr.notify.domain.entity.Template;

import java.util.Map;

public record TemplateResponse(
    String id,
    String name,
    Map<String, String> templateVariables
) {
    public static TemplateResponse from(Template template) {
        return new TemplateResponse(template.getId(), template.getName(), template.getTemplateVariables());
    }
}