package com.aimr.notify.model.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterTenantRequest {
    @NotBlank(message = "name is required")
    private String name;
}