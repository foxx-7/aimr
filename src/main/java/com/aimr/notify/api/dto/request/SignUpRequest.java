package com.aimr.notify.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
    String userName,
    @NotBlank(message = "email is required")     @Email(message = "invalid email format") String email,
    @NotBlank(message = "password is required") String passwordString
) {
}