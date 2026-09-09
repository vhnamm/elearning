package com.hnv.elearning.feature.auth.service;

import com.hnv.elearning.feature.auth.dto.LoginRequest;
import com.hnv.elearning.feature.auth.dto.LoginResponse;
import com.hnv.elearning.feature.auth.dto.RegisterRequest;

public interface AuthenticationService {
    LoginResponse login(LoginRequest loginRequest, String clientIp, String userAgent);
    void register(RegisterRequest registerRequest);
}
