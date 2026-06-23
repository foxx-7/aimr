package com.aimr.notify.api.dto.response;

import com.aimr.notify.domain.entity.Tenant;

public record TenantResponse(
    String name,
    String ownerId
) {

    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
            tenant.getName(),
            tenant.getOwnerId()
        );
    }
}