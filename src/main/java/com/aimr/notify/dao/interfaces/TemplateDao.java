package com.aimr.notify.dao.interfaces;

import com.aimr.notify.model.entity.Template;
import lombok.NonNull;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.function.Supplier;

public interface TemplateDao {

    Optional<Template> findTemplateByTenantIdAndName(String tenantId, String templateName);

    Optional<Template> findTemplateByTenantIdAndId(String tenantId, String id);

    Template saveTemplate(Template template);

    Page<@NonNull Template> filterTemplate(Example<@NonNull Template> example,
                                           PageRequest pageRequest);

    void deleteTemplateById(String id, Supplier<? extends Throwable> exceptionHandler);
}
