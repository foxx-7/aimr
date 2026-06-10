package com.aimr.notify.dao.impl;

import com.aimr.notify.dao.interfaces.CacheService;
import com.aimr.notify.dao.interfaces.TemplateDao;
import com.aimr.notify.dao.repostiories.mongo.TemplateRepository;
import com.aimr.notify.models.entity.Template;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Supplier;

import static com.aimr.notify.util.CommonUtils.getCurrentTenantId;
import static com.aimr.notify.util.CommonUtils.isNotEmpty;


@Service
@Slf4j
@RequiredArgsConstructor
class TemplateDaoImpl implements TemplateDao {

    private final TemplateRepository templateRepository;
    private final CacheService cacheService;

/*
        implementations custom to entity class type
        prefix concat should include with second arg
 */

    @Override
    public Optional<Template> findTemplateByTenantIdAndName(final String tenantId, final String templateName) {
        return cacheService.getByName(tenantId, templateName,Template.class )
                .or(() -> templateRepository.findByNameIgnoreCaseAndTenantId(templateName, tenantId)
                        .map(template -> {
                            cacheService.putByName(tenantId, templateName, template);
                            return template;
                        }));
    }

    @Override
    public Optional<Template> findTemplateByTenantIdAndId(final String tenantId, final String id) {
        return cacheService.getById(tenantId, id, Template.class).or(() -> {
            return templateRepository.findByIdAndTenantId(id, tenantId).map(template -> {
                cacheService.putById(tenantId, id, template);
                return template;
            });
        });
    }

    @Override
    public Template saveTemplate(final Template template) {
        cacheService.putById(template.getTenantId(), template.getId(), template);
        cacheService.putByName(template.getTenantId(), template.getName(), template);
        return templateRepository.save(template);
    }

    @Override
    public Page<@NonNull Template> filterTemplate(final Example<@NonNull Template> example,
                                                  final PageRequest pageRequest) {
        return templateRepository.findAll(example, pageRequest);
    }

    @Override
    public void deleteTemplateById(final String id, final Supplier<? extends Throwable> exceptionHandler) {
        findTemplateByTenantIdAndId(getCurrentTenantId(), id).ifPresentOrElse(template -> {
            cacheService.deleteById(id, template.getId(), Template.class);
            cacheService.deleteByName(template.getTenantId(), template.getName(), Template.class);
            templateRepository.deleteById(id);
        }, () -> {
            if (isNotEmpty(exceptionHandler)) {
                exceptionHandler.get();
            }
        });
    }
}
