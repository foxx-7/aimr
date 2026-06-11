package com.aimr.notify.service.impl;

import com.aimr.notify.dao.interfaces.TemplateDao;
import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.model.entity.Template;
import com.aimr.notify.aop.ValidateTenant;
import com.aimr.notify.model.dto.request.CreateTemplateRequest;
import com.aimr.notify.model.dto.request.UpdateTemplateRequest;
import com.aimr.notify.model.dto.request.TemplateFilterRequest;
import com.aimr.notify.model.dto.response.FilterTemplateResponse;
import com.aimr.notify.model.dto.response.TemplateResponse;
import com.aimr.notify.model.dto.response.TemplateResponseDTO;
import com.aimr.notify.service.interfaces.TemplateService;
import com.aimr.notify.util.CommonUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.aimr.notify.constant.ErrorConstants.*;
import static com.aimr.notify.util.CommonUtils.*;


@Service
@Slf4j
@RequiredArgsConstructor
class TemplateServiceImpl implements TemplateService {

    private final TemplateDao templateDao;

    @Override
    @ValidateTenant
    public TemplateResponse createTemplate(final CreateTemplateRequest request) {
//        NotificationContext context = NotificationContextHolder.getContext();

        templateDao.findTemplateByTenantIdAndName(getCurrentTenantId(), request.getName()).ifPresent(_ -> {
            throw new ValidationException(TEMPLATE_ALREADY_EXISTS_ERROR, HttpStatus.BAD_REQUEST.value());
        });

        Template template = new Template();
        template.setId(CommonUtils.generateUUIDv7());
        template.setTenantId(getCurrentTenantId());
        BeanUtils.copyProperties(request, template);
        template.entityCreated();
        templateDao.saveTemplate(template);
        return new TemplateResponse(template);
    }

    @Override
    @ValidateTenant
    public FilterTemplateResponse filterTemplate(TemplateFilterRequest request) {
//        NotificationContextHolder.ignoreTenantIdInjection();
        Page<@NonNull Template> templates = templateDao.filterTemplate(request.buildSearch(), request.buildPageRequest());
        List<TemplateResponseDTO> data = templates.stream().map(TemplateResponseDTO::new).toList();
        return new FilterTemplateResponse(data, templates.hasNext(), templates.getTotalElements());
    }

    @Override
    @ValidateTenant
    public TemplateResponse updateTemplate(String id, UpdateTemplateRequest request) {
        Template template = getTemplateForCurrentTenant(id);
        if (isNotEmpty(request.getName())) {
            templateDao.findTemplateByTenantIdAndName(getCurrentTenantId(), request.getName()).ifPresent(_ -> {
                throw new ValidationException(TEMPLATE_ALREADY_EXISTS_ERROR, HttpStatus.BAD_REQUEST.value());
            });
            template.setName(request.getName());
        }
        if (isNotEmpty(request.getMessageTemplate())) {
            template.setMessageTemplate(request.getMessageTemplate());
        }
        if (isNotEmpty(request.getTemplateVariables())) {
            template.setTemplateVariables(request.getTemplateVariables());
        }
        templateDao.saveTemplate(template);
        return new TemplateResponse(template);
    }

    @Override
    @ValidateTenant
    public void deleteTemplate(final String id) {
        templateDao.deleteTemplateById(id, () -> new ValidationException(TEMPLATE_NOT_FOUND_ERROR,
                HttpStatus.BAD_REQUEST.value()));
    }

    @Override
    @ValidateTenant
    public Template getTemplateForCurrentTenant(final String id) {
       return templateDao.findTemplateByTenantIdAndId(getCurrentTenantId(), id)
                .orElseThrow(() -> new ValidationException(TENANT_NOT_FOUND_ERROR,
                        HttpStatus.BAD_REQUEST.value()));
    }
}