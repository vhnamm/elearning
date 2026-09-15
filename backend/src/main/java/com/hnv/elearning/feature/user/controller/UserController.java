package com.hnv.elearning.feature.user.controller;

import com.hnv.elearning.common.response.ApiResponse;
import com.hnv.elearning.feature.auth.dto.LoginResponse;
import com.hnv.elearning.feature.user.dto.OnboardingRequest;
import com.hnv.elearning.feature.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/instructors/onboarding")
    public ResponseEntity<ApiResponse<LoginResponse>> instructorOnboarding(@RequestBody OnboardingRequest onboardingRequest) {
        LoginResponse loginResponse = userService.instructorOnboarding(onboardingRequest);
        ApiResponse<LoginResponse> apiResponse = ApiResponse.<LoginResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Successfully onboarding")
                .data(loginResponse)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }
}
