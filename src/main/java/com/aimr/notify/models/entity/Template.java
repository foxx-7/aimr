package com.aimr.notify.models.entity;

import com.aimr.notify.models.interfaces.TenantAware;
import com.aimr.notify.models.entity.annotations.CachePrefix;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "templates")
@CachePrefix("TEMPLATE.")
public class Template extends AbstractEntity implements TenantAware {
    private String id;
    private String name;
    private Map<String, String> templateVariables;
    private String messageTemplate;
    private String tenantId;
}
