package com.aimr.notify.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RegisterTenantRequest(
    @NotBlank(message = "name is required") String name
) {
}