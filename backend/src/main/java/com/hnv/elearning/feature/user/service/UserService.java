package com.hnv.elearning.feature.user.service;

import com.hnv.elearning.feature.auth.dto.LoginResponse;
import com.hnv.elearning.feature.user.dto.OnboardingRequest;

public interface UserService {
    LoginResponse instructorOnboarding(OnboardingRequest onboardingRequest);
}
