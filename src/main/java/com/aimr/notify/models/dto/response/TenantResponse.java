package com.aimr.notify.models.dto.response;

import com.aimr.notify.models.entity.Tenant;
import lombok.Data;

@Data
public class TenantResponse{
    private String name;
    private String ownerId;

    public TenantResponse(Tenant tenant){
        setName(tenant.getName());
        setOwnerId(tenant.getOwnerId());
    }
}
