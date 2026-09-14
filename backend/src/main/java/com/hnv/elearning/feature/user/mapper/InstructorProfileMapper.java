package com.hnv.elearning.feature.user.mapper;

import com.hnv.elearning.feature.user.dto.OnboardingRequest;
import com.hnv.elearning.feature.user.entity.InstructorProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface InstructorProfileMapper {

    InstructorProfile toEntity(OnboardingRequest onboardingRequest);
}
