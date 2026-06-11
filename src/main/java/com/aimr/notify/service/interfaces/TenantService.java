package com.aimr.notify.service.interfaces;

import com.aimr.notify.aop.ValidateTenant;
import com.aimr.notify.model.dto.request.RegisterTenantRequest;
import com.aimr.notify.model.dto.response.TenantResponse;

import java.util.Map;

public interface TenantService {

    TenantResponse registerTenant(String ownerId, RegisterTenantRequest request);

    void deleteTenant(String id, String ownerId);

    TenantResponse  updateTenant(String id, String ownerId, Map<String, Object> update);

    @ValidateTenant
    String generateNewApiKey(String name, String userId);
}
