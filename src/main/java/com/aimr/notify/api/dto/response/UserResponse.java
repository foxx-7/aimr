package com.aimr.notify.api.dto.response;

import com.aimr.notify.domain.entity.User;

public record UserResponse(
    String email
) {

    public static UserResponse from(User user) {
        return new UserResponse(
            user.getEmail()
        );
    }
}