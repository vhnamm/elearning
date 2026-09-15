package com.hnv.elearning.feature.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OnboardingRequest {
    private String email;
    private String experience;
    private String headline;
    private String bio;
}
