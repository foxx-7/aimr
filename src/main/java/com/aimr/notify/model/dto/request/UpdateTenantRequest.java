package com.aimr.notify.model.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.util.Map;

@Data
public class UpdateTenantRequest {
    private String name;
    @Email(message = "invalid email format")
    private String email;
    private Map<String, String> properties;
}
