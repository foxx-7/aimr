package com.aimr.notify.model.dto.request;

import com.aimr.notify.model.entity.Template;
import com.aimr.notify.model.dto.BaseSearchDTO;
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
