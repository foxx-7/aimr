package com.aimr.notify.model.dto.response;

import com.aimr.notify.model.entity.User;
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
