package com.aimr.notify.model.dto.response;

import com.aimr.notify.model.entity.Template;
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
