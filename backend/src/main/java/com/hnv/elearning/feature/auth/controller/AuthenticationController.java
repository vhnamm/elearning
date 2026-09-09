package com.hnv.elearning.feature.auth.controller;

import com.hnv.elearning.common.response.ApiResponse;
import com.hnv.elearning.common.utils.CookieUtil;
import com.hnv.elearning.common.utils.HeaderUtil;
import com.hnv.elearning.feature.auth.dto.LoginRequest;
import com.hnv.elearning.feature.auth.dto.LoginResponse;
import com.hnv.elearning.feature.auth.dto.RegisterRequest;
import com.hnv.elearning.feature.auth.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Value("${REFRESH_EXPIRATION}")
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
        return null;
    }
}
