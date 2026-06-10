package com.aimr.notify.service.impl;

import com.aimr.notify.dao.interfaces.UserDao;
import com.aimr.notify.exception.ResourceNotFoundException;
import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.models.dto.request.LoginRequest;
import com.aimr.notify.models.dto.request.SignUpRequest;
import com.aimr.notify.models.dto.response.AuthResponse;
import com.aimr.notify.models.dto.response.SignUpResponse;
import com.aimr.notify.models.entity.User;
import com.aimr.notify.security.util.JwtUtil;
import com.aimr.notify.service.interfaces.AuthService;
import com.aimr.notify.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.aimr.notify.constants.ErrorConstants.EMAIL_ALREADY_IN_USE_ERROR;
import static com.aimr.notify.constants.ErrorConstants.USER_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public SignUpResponse registerUser(SignUpRequest request) {
        userDao.findByEmail(request.getEmail()).ifPresent(_ -> {
            throw new ValidationException(EMAIL_ALREADY_IN_USE_ERROR, HttpStatus.BAD_REQUEST.value());
        });

        User user = User.builder()
                .id(CommonUtils.generateUUIDv7())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPasswordString()))
                .build();

        userDao.save(user);

        log.info("New user registered: {}", user.getEmail());

        return new SignUpResponse(user.getEmail(), "signup success");
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        // authenticate with email and password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userDao.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_ERROR, HttpStatus.NOT_FOUND.value()));

        //should fetch user memberships and embed in  auth response;
        return AuthResponse.builder()
                .token(jwtUtil.generateToken(user.getId(), user.getEmail()))
                .email(user.getEmail())
                .build();
    }
}
