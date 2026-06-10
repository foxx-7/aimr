package com.aimr.notify.service.interfaces;

import com.aimr.notify.models.dto.request.LoginRequest;
import com.aimr.notify.models.dto.request.SignUpRequest;
import com.aimr.notify.models.dto.response.AuthResponse;
import com.aimr.notify.models.dto.response.SignUpResponse;

public interface AuthService {

    SignUpResponse registerUser(SignUpRequest request);

    AuthResponse login(LoginRequest request);

}
