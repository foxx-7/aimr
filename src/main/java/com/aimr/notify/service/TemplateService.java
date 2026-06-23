package com.aimr.notify.service;

import com.aimr.notify.infra.mongo.dao.TemplateDao;
import com.aimr.notify.exception.ResourceNotFoundException;
import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.api.dto.response.MongoSearchResult;
import com.aimr.notify.api.dto.request.TemplateSearchDTO;
import com.aimr.notify.domain.entity.Template;
import com.aimr.notify.domain.aop.annotation.ValidateTenant;
import com.aimr.notify.api.dto.request.CreateTemplateRequest;
import com.aimr.notify.api.dto.request.UpdateTemplateRequest;
import com.aimr.notify.api.dto.response.TemplateResponse;
import com.aimr.notify.util.CommonUtils;
import com.aimr.notify.api.util.EntityUpdater;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


import static com.aimr.notify.constant.ErrorConstants.TEMPLATE_ALREADY_EXISTS_ERROR;
import static com.aimr.notify.constant.ErrorConstants.TENANT_NOT_FOUND_ERROR;
import static com.aimr.notify.util.CommonUtils.getCurrentTenantId;


@Service
@Slf4j
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateDao templateDao;

    @ValidateTenant
    public TemplateResponse createTemplate(final CreateTemplateRequest request) {
//        NotificationContext context = NotificationContextHolder.getContext();

        templateDao.fetchTemplateByTenantIdAndName(getCurrentTenantId(), request.name()).ifPresent(_ -> {
            throw new ValidationException(TEMPLATE_ALREADY_EXISTS_ERROR, HttpStatus.BAD_REQUEST.value());
        });

        Template template = new Template();
        template.setId(CommonUtils.generateUUIDv7());
        template.setTenantId(getCurrentTenantId());
        BeanUtils.copyProperties(request, template);
        template.entityCreated();
        templateDao.saveEntity(template);
        return TemplateResponse.from(template);
    }

    @ValidateTenant
    public MongoSearchResult<TemplateResponse> filterTemplate(TemplateSearchDTO searchDTO) {
        String tenantId = getCurrentTenantId();
        return templateDao.filterTemplate(tenantId,searchDTO);
    }

    @ValidateTenant
    public TemplateResponse updateTemplate(String id, UpdateTemplateRequest request) {
        Template template = EntityUpdater.updateForTenant(
                templateDao, getCurrentTenantId(), id, request);
        return TemplateResponse.from(template);
    }

    @ValidateTenant
    public void deleteTemplate(final String id) {
        templateDao.fetchTemplateByTenantIdAndId(getCurrentTenantId(), id)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Template with provided details not found"));
    }

    @ValidateTenant
    public Template getTemplateForCurrentTenant(final String id) {
       return templateDao.fetchTemplateByTenantIdAndId(getCurrentTenantId(), id)
                .orElseThrow(() -> new ValidationException(TENANT_NOT_FOUND_ERROR,
                        HttpStatus.BAD_REQUEST.value()));
    }
}