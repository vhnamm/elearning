package com.hnv.elearning.feature.auth.service;

import com.hnv.elearning.feature.auth.dto.LoginRequest;
import com.hnv.elearning.feature.auth.dto.LoginResponse;

public interface AuthenticationService {
    LoginResponse login(LoginRequest loginRequest, String clientIp, String userAgent);
}
