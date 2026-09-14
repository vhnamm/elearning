package com.hnv.elearning.feature.user.service.impl;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import com.hnv.elearning.feature.auth.dto.LoginResponse;
import com.hnv.elearning.feature.user.dto.OnboardingRequest;
import com.hnv.elearning.feature.user.entity.InstructorProfile;
import com.hnv.elearning.feature.user.entity.Role;
import com.hnv.elearning.feature.user.entity.User;
import com.hnv.elearning.feature.user.entity.UserRole;
import com.hnv.elearning.feature.user.mapper.InstructorProfileMapper;
import com.hnv.elearning.feature.user.repository.InstructorProfileRepository;
import com.hnv.elearning.feature.user.repository.RoleRepository;
import com.hnv.elearning.feature.user.repository.UserRepository;
import com.hnv.elearning.feature.user.repository.UserRoleRepository;
import com.hnv.elearning.feature.user.service.UserService;
import com.hnv.elearning.security.jwt.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final InstructorProfileRepository instructorProfileRepository;
    private final InstructorProfileMapper instructorProfileMapper;
    private final RoleRepository roleRepository;
    private final UserRoleRepository  userRoleRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public LoginResponse instructorOnboarding(OnboardingRequest onboardingRequest) {
        String email = onboardingRequest.getEmail();
        boolean exist = instructorProfileRepository.existsByEmail(email);
        if(exist){
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Role role = roleRepository.findByName("INSTRUCTOR").orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        InstructorProfile instructorProfile = instructorProfileMapper.toEntity(onboardingRequest);
        instructorProfile.setUser(user);

        instructorProfileRepository.save(instructorProfile);
        UserRole userRole = UserRole.builder()
                .role(role)
                .user(user)
                .build();

        userRoleRepository.save(userRole);

        String accessToken = jwtProvider.generateJwtToken(user);
        return LoginResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}
