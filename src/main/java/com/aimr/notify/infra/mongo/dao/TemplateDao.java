package com.aimr.notify.infra.mongo.dao;

import com.aimr.notify.infra.redis.cache.CacheService;
import com.aimr.notify.domain.dao.UpdatableDao;
import com.aimr.notify.infra.mongo.repo.TemplateRepository;
import com.aimr.notify.api.dto.response.MongoSearchResult;
import com.aimr.notify.api.dto.request.TemplateSearchDTO;
import com.aimr.notify.api.dto.response.TemplateResponse;
import com.aimr.notify.domain.entity.Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TemplateDao implements UpdatableDao<Template> {

    private final TemplateRepository templateRepository;
    private final CacheService cacheService;
    private final MongoTemplate mongoTemplate;

    public Optional<Template> fetchTemplateByTenantIdAndName(final String tenantId, final String templateName) {
        return cacheService.getByName(tenantId, templateName,Template.class )
                .or(() -> templateRepository.findByNameIgnoreCaseAndTenantId(templateName, tenantId)
                        .map(template -> {
                            cacheService.putByName(tenantId, templateName, template);
                            return template;
                        }));
    }

    @Override
    public Optional<Template> fetchEntity(final String tenantId, final String id) {
       return fetchTemplateByTenantIdAndId(tenantId, id);
    }

    public Optional<Template> fetchTemplateByTenantIdAndId(final String tenantId, final String id){
        return cacheService.getById(tenantId, id, Template.class).or(() ->
                templateRepository.findByIdAndTenantId(id, tenantId).map(template -> {
            cacheService.putById(tenantId, id, template);
            return template;
        }));
    }

    @Override
    public Template saveEntity(final Template template) {
        return save(template);
    }

    private Template save(final Template template){
        cacheService.putById(template.getTenantId(), template.getId(), template);
        cacheService.putByName(template.getTenantId(), template.getName(), template);
        return templateRepository.save(template);
    }

    public void deleteTemplate(final Template template){
        templateRepository.delete(template);
    }

    public MongoSearchResult<TemplateResponse> filterTemplate(
            String tenantId,
            TemplateSearchDTO searchDTO){

        Query query = searchDTO.toQuery(tenantId);
        List<Template> results = mongoTemplate.find(query, Template.class);

        int pageSize = searchDTO.resolvedPageSize();
        boolean hasMore = results.size() > pageSize;
        List<Template> page = hasMore ? results.subList(0, pageSize) : results;

        // For templates, the client passes back the raw anchorTime from the last
        // document rather than an opaque cursor — simpler because timestamp
        // collisions are not a concern for templates.
        return MongoSearchResult.of(
                page.stream().map(TemplateResponse::from).toList(),
                page.size(), 0, pageSize,
                hasMore ? page.getLast().getCreatedAt().toString() : null
        );

    }

}
