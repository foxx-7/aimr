package com.aimr.notify.models.dto.response;

import com.aimr.notify.models.entity.User;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
public class AuthenticatedUserDetails implements UserDetails , CredentialsContainer {
    private String userId;
    private String email;
    private String password;

    public AuthenticatedUserDetails(final User user){
        setUserId(user.getId());
        setEmail(user.getEmail());
        setPassword(user.getPasswordHash());
    }

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
        return email;
    }

    @Override
    public void eraseCredentials(){
        password = null;
    }
}
