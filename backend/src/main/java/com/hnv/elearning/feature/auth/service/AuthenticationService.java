package com.hnv.elearning.feature.auth.service;

import com.hnv.elearning.feature.auth.dto.ConfirmOtpRequest;
import com.hnv.elearning.feature.auth.dto.LoginRequest;
import com.hnv.elearning.feature.auth.dto.LoginResponse;
import com.hnv.elearning.feature.auth.dto.RegisterRequest;
import com.hnv.elearning.feature.user.entity.User;

public interface AuthenticationService {
    LoginResponse login(LoginRequest loginRequest, String clientIp, String userAgent);
    void register(RegisterRequest registerRequest);
    void confirmOtp(ConfirmOtpRequest confirmOtpRequest);
    void logout(String refreshToken, String accessToken);
    LoginResponse refresh(String refreshToken);
    User processGoogleLogin(String email, String fullName, String avatarUrl, String providerId);
}
