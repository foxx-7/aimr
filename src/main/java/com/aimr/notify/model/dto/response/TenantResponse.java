package com.aimr.notify.model.dto.response;

import com.aimr.notify.model.entity.Tenant;
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
