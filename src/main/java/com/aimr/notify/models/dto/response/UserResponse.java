package com.aimr.notify.models.dto.response;

import com.aimr.notify.models.entity.User;
import com.aimr.notify.models.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private String email;

    public UserResponse(User user){
        setEmail(user.getEmail());
    }
}
