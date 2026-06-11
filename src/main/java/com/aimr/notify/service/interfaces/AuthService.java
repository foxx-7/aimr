package com.aimr.notify.service.interfaces;

import com.aimr.notify.model.dto.request.LoginRequest;
import com.aimr.notify.model.dto.request.SignUpRequest;
import com.aimr.notify.model.dto.response.AuthResponse;
import com.aimr.notify.model.dto.response.SignUpResponse;

public interface AuthService {

    SignUpResponse registerUser(SignUpRequest request);

    AuthResponse login(LoginRequest request);

}
