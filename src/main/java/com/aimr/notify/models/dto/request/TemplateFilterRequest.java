package com.aimr.notify.models.dto.request;

import com.aimr.notify.models.entity.Template;
import com.aimr.notify.models.dto.BaseSearchDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TemplateFilterRequest extends BaseSearchDTO<Template> {

    private String name;

    @Override
    public Class<Template> getEntity() {
        return Template.class;
    }
}
