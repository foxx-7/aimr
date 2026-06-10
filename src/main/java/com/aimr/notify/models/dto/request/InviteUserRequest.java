package com.aimr.notify.models.dto.request;

import com.aimr.notify.models.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InviteUserRequest {

    @NotBlank(message = "email is required")
    @Email(message = "invalid email format")
   private String email;

    @NotBlank(message = "user role must be specified")
   private Role role;

}
