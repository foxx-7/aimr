package com.aimr.notify.api.dto.response;

import com.aimr.notify.domain.entity.User;
import lombok.Builder;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Builder
public record AuthenticatedUserDetails(
    String userName,
    String userId,
    String password
) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    public static AuthenticatedUserDetails from(User user){
        return AuthenticatedUserDetails.builder()
                .userId(user.getId())
                .userName(user.getEmail())
                .password(user.getPasswordHash())
                .build();
    }
}
