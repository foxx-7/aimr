package com.aimr.notify.dao.repostiories.mongo;

import com.aimr.notify.model.entity.Template;
import lombok.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemplateRepository extends MongoRepository<@NonNull Template, @NonNull String> {

    Optional<Template> findByNameIgnoreCaseAndTenantId(String name, String tenantId);

    Optional<Template> findByIdAndTenantId(String id, String tenantId);

}
