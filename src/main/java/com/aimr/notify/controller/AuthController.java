package com.aimr.notify.controller;

import com.aimr.notify.model.dto.request.LoginRequest;
import com.aimr.notify.model.dto.request.SignUpRequest;
import com.aimr.notify.model.dto.response.ApiResponse;
import com.aimr.notify.model.dto.response.AuthResponse;
import com.aimr.notify.model.dto.response.SignUpResponse;
import com.aimr.notify.service.interfaces.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.aimr.notify.constant.ApplicationConstants.OBJECT_CREATED_SUCCESS_MESSAGE;

@RestController
@RequestMapping("/api/v1/auth/user")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<SignUpResponse> signUp(@RequestBody @Valid SignUpRequest request){
        return ApiResponse.success(HttpStatus.CREATED.value(),
                OBJECT_CREATED_SUCCESS_MESSAGE, authService.registerUser(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody @Valid LoginRequest request){
        return ApiResponse.success(HttpStatus.OK.value(),
                "login successful", authService.login(request));
    }
}