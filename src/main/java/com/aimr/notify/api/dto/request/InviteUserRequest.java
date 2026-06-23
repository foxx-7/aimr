package com.aimr.notify.api.dto.request;

import com.aimr.notify.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InviteUserRequest(
    @NotBlank(message = "email is required")     @Email(message = "invalid email format") String email,
    @NotBlank(message = "user role must be specified") Role role
) {
}