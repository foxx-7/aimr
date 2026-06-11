package com.aimr.notify.service.interfaces;


import com.aimr.notify.model.entity.Template;
import com.aimr.notify.model.dto.request.CreateTemplateRequest;
import com.aimr.notify.model.dto.request.UpdateTemplateRequest;
import com.aimr.notify.model.dto.request.TemplateFilterRequest;
import com.aimr.notify.model.dto.response.FilterTemplateResponse;
import com.aimr.notify.model.dto.response.TemplateResponse;

public interface TemplateService {
    TemplateResponse createTemplate(CreateTemplateRequest request);

    FilterTemplateResponse filterTemplate(TemplateFilterRequest request);

    TemplateResponse updateTemplate(String id, UpdateTemplateRequest request);

    void deleteTemplate(String id);

    Template getTemplateForCurrentTenant(String id);
}
