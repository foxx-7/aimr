package com.aimr.notify.service;

import com.aimr.notify.infra.postgres.dao.RefreshTokenDao;
import com.aimr.notify.infra.postgres.dao.UserDao;
import com.aimr.notify.exception.ResourceNotFoundException;
import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.api.dto.request.LoginRequest;
import com.aimr.notify.api.dto.request.SignUpRequest;
import com.aimr.notify.api.dto.response.AuthResponse;
import com.aimr.notify.api.dto.response.SignUpResponse;
import com.aimr.notify.domain.entity.RefreshToken;
import com.aimr.notify.domain.entity.User;
import com.aimr.notify.api.security.util.JwtUtil;
import com.aimr.notify.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.aimr.notify.constant.ErrorConstants.EMAIL_ALREADY_IN_USE_ERROR;
import static com.aimr.notify.constant.ErrorConstants.USER_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenDao refreshTokenDao;

    public SignUpResponse registerUser(SignUpRequest request) {
        userDao.fetchUserByEmail(request.email()).ifPresent(_ -> {
            throw new ValidationException(EMAIL_ALREADY_IN_USE_ERROR, HttpStatus.BAD_REQUEST.value());
        });

        User user = User.builder()
                .id(CommonUtils.generateUUIDv7())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.passwordString()))
                .build();

        userDao.saveUser(user);

        log.info("New user registered: {}", user.getEmail());

        return new SignUpResponse(user.getEmail(), "signup success");
    }

    public AuthResponse login(LoginRequest request) {

        // authenticate with email and password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userDao.fetchUserByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_ERROR, HttpStatus.NOT_FOUND.value()));

        //generate refresh token and store;
        updateRefreshToken(user.getId());

        //should fetch user memberships and embed in  auth response;
        return AuthResponse.builder()
                .token(jwtUtil.generateToken(user.getId(), user.getEmail()))
                .email(user.getEmail())
                .build();
    }

    public void updateRefreshToken(final String userId){
        refreshTokenDao.fetchToken(userId).forEach(token -> {
            token.setRevoked(true);
            refreshTokenDao.saveToken(token);
        });
        RefreshToken newToken = RefreshToken.builder()
                .id(CommonUtils.generateUUIDv7())
                .userId(userId)
                .tokenHash(CommonUtils.generateSHA256Hash(CommonUtils.generateUUIDv4()))
                .revoked(false)
                .build();
        refreshTokenDao.saveToken(newToken);
    }


}
