package com.hnv.elearning.feature.auth.service.impl;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import com.hnv.elearning.feature.auth.dto.LoginRequest;
import com.hnv.elearning.feature.auth.dto.LoginResponse;
import com.hnv.elearning.feature.auth.dto.PendingUserDto;
import com.hnv.elearning.feature.auth.dto.RegisterRequest;
import com.hnv.elearning.feature.user.entity.User;
import com.hnv.elearning.feature.user.repository.UserRepository;
import com.hnv.elearning.infrastructure.redis.RedisService;
import com.hnv.elearning.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements com.hnv.elearning.feature.auth.service.AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final RedisService redisService;

    @Value("${otp.expiration}")
    private long pending_ttl;

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
        boolean exist = userRepository.existsByEmail(registerRequest.getEmail());
        if(exist){
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }
        String password = passwordEncoder.encode(registerRequest.getPassword());

        PendingUserDto pendingUserDto = PendingUserDto.builder()
                .email(registerRequest.getEmail())
                .passwordHash(password)
                .fullName(registerRequest.getFullName())
                .build();


        redisService.set("auth:pending-user", pendingUserDto, Duration.ofMillis(pending_ttl));

    }
}
