package com.aimr.notify.api.dto.request;

import java.util.Map;

public record UpdateTenantRequest(
    String name,
    String email,
    Map<String, String> properties
) {
}