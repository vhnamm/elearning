package com.hnv.elearning.feature.auth.controller;

import com.hnv.elearning.common.response.ApiResponse;
import com.hnv.elearning.common.utils.CookieUtil;
import com.hnv.elearning.common.utils.HeaderUtil;
import com.hnv.elearning.feature.auth.dto.ConfirmOtpRequest;
import com.hnv.elearning.feature.auth.dto.LoginRequest;
import com.hnv.elearning.feature.auth.dto.LoginResponse;
import com.hnv.elearning.feature.auth.dto.RegisterRequest;
import com.hnv.elearning.feature.auth.dto.UserResponse;
import com.hnv.elearning.feature.auth.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Value("${jwt.refresh-expiration}")
    private Long REFRESH_EXPIRATION;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        String clientIp = HeaderUtil.getClientIp(request);
        String userAgent = HeaderUtil.getUserAgent(request);
        LoginResponse loginResponse = authenticationService.login(loginRequest, clientIp, userAgent);

        CookieUtil.addCookie(response,
                "refreshToken",
                loginResponse.getRefreshToken(),
                "/api/v1/auth",
                REFRESH_EXPIRATION,
                true
        );

        ApiResponse<LoginResponse> apiResponse = ApiResponse.<LoginResponse>builder()
                .code(HttpStatus.OK.value())
                .data(loginResponse)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @RequestBody RegisterRequest registerRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authenticationService.register(registerRequest);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .message("Vui lòng kiểm tra mã OTP được gửi về hòm thư của bạn")
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping("/confirm-otp")
    public ResponseEntity<ApiResponse<Void>> confirmOtp(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody ConfirmOtpRequest confirmOtpRequest
    ) {
        authenticationService.confirmOtp(confirmOtpRequest);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .message("Tạo tài khoản thành công")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            @CookieValue(name = "refreshToken") String refreshToken
    ) {
        String accessToken = HeaderUtil.extractBearerToken(request);

        authenticationService.logout(refreshToken, accessToken);

        CookieUtil.deleteCookie(response, "refreshToken", "/api/v1/auth");

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .message("Đăng xuất thành công")
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            HttpServletRequest request,
            HttpServletResponse response,
            @CookieValue(name = "refreshToken") String refreshToken
    ) {
        LoginResponse loginResponse = authenticationService.refresh(refreshToken);
        ApiResponse<LoginResponse> apiResponse = ApiResponse.<LoginResponse>builder()
                .message("Làm mới access token thành công")
                .data(loginResponse)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me() {
        UserResponse userResponse = authenticationService.getCurrentUser();
        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .data(userResponse)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

}
