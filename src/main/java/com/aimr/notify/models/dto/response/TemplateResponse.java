package com.aimr.notify.models.dto.response;

import com.aimr.notify.models.entity.Template;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateResponse {

    private String id;
    private String name;

    public TemplateResponse(final Template template) {
        setId(template.getId());
        setName(template.getName());
    }
}
