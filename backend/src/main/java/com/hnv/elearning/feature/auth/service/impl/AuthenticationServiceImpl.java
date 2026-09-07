package com.hnv.elearning.feature.auth.service.impl;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import com.hnv.elearning.feature.auth.dto.LoginRequest;
import com.hnv.elearning.feature.auth.dto.LoginResponse;
import com.hnv.elearning.feature.auth.dto.RegisterRequest;
import com.hnv.elearning.feature.user.entity.User;
import com.hnv.elearning.feature.user.repository.UserRepository;
import com.hnv.elearning.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements com.hnv.elearning.feature.auth.service.AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    public LoginResponse login(LoginRequest loginRequest, String clientIp, String userAgent) {
        log.info("Login Request: {}", loginRequest);

        User user = userRepository.findWithAuthortiesByEmail(loginRequest.getEmail()).orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if(user.getPassword() == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
            user, null,user.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtProvider.generateJwtToken(user);
        String refreshToken = refreshTokenService.create(user, clientIp, userAgent);

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return loginResponse;

    }

    public void register(RegisterRequest registerRequest) {
        log.info("Register Request: {}", registerRequest);

    }
}
