package com.hnv.elearning.feature.auth.service.impl;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import com.hnv.elearning.common.otp.OtpService;
import com.hnv.elearning.feature.auth.dto.*;
import com.hnv.elearning.feature.auth.service.AuthMailService;
import com.hnv.elearning.feature.user.entity.Role;
import com.hnv.elearning.feature.user.entity.User;
import com.hnv.elearning.feature.user.entity.UserRole;
import com.hnv.elearning.feature.user.repository.RoleRepository;
import com.hnv.elearning.feature.user.repository.UserRepository;
import com.hnv.elearning.feature.user.repository.UserRoleRepository;
import com.hnv.elearning.infrastructure.redis.RedisService;
import com.hnv.elearning.security.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements com.hnv.elearning.feature.auth.service.AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final RedisService redisService;
    private final AuthMailService authMailService;
    private final OtpService otpService;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

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


    @Override
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


        redisService.set("auth:pending-user:" + registerRequest.getEmail(), pendingUserDto, Duration.ofMillis(pending_ttl));
        String otp = otpService.generateOtp();
        log.info("OTP: {}", otp);

        redisService.set("auth:otp:" + registerRequest.getEmail(), passwordEncoder.encode(otp), Duration.ofMillis(pending_ttl));

        //mail otp ve email
        authMailService.sendAuthMail(registerRequest.getEmail(), "[Mót Edu] Mã xác thực tài khoản của bạn", otp);

        return;
    }

    @Transactional
    @Override
    public void confirmOtp(ConfirmOtpRequest confirmOtpRequest){
        log.info("Confirm Otp Request: {}", confirmOtpRequest.getEmail() + " " +  confirmOtpRequest.getOtp());
        //check redis
        String email =  confirmOtpRequest.getEmail();
        otpService.verifyOtp("auth:otp:" + email, confirmOtpRequest.getOtp());

       PendingUserDto pendingUserDto = redisService.get("auth:pending-user:" + email, PendingUserDto.class);
       if(pendingUserDto == null){
           throw new AppException(ErrorCode.OTP_INVALID);
       }

        Role role = roleRepository.findByName("STUDENT").orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

       User user = User.builder()
               .email(pendingUserDto.getEmail())
               .fullName(pendingUserDto.getFullName())
               .password(pendingUserDto.getPasswordHash())
               .build();

        User savedUser;
        //tranh concurency: ep flush ngay de bat duoc loi trung email tu unique constraint
        try{
            savedUser = userRepository.saveAndFlush(user);
        }catch (DataIntegrityViolationException e){
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        UserRole userRole = UserRole.builder()
               .user(savedUser)
               .role(role)
               .build();
        userRoleRepository.save(userRole);

        redisService.delete("auth:pending-user:" + email);
    }

    @Override
    @Transactional
    public void logout(String refreshToken, String accessToken){
        log.info("Logout Request: {}", refreshToken, accessToken);
        if(accessToken != null && !accessToken.isEmpty()){
            try{
                Claims claims = jwtProvider.parseClaims(accessToken);
                String jti = (String) claims.get("jti");
                Instant expiration = claims.getExpiration().toInstant();

                Duration remaining = Duration.between(Instant.now(), expiration);
                if(!remaining.isNegative() && !remaining.isZero()){
                    redisService.set("auth:blacklist:" + jti, true, remaining);
                }

            }catch (ExpiredJwtException e){
                log.info("Expired JWT Token: {}", accessToken);
            }catch (JwtException e){
                log.info("Expired JWT Token: {}", accessToken);
            }

            if(refreshToken != null &&  !refreshToken.isBlank()){
                refreshTokenService.revoke(refreshToken);
            }
        }
        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional
    public LoginResponse refresh(String refreshToken){
        if(refreshToken == null && !refreshToken.isEmpty()){
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        User user = refreshTokenService.validate(refreshToken);
        //tao accessToken moi
        String newAccessToken = jwtProvider.generateJwtToken(user);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    @Override
    @Transactional
    public User processGoogleLogin(String email, String fullName, String avatarUrl, String providerId) {
        Optional<User> existingOpt = userRepository.findWithAuthortiesByEmail(email);

        if (existingOpt.isPresent()) {
            User existing = existingOpt.get();
            if (existing.getGoogleId() == null) {
                // Case: da dang ky local tu truoc, chua tung login Google -> link account
                existing.setGoogleId(providerId);
                if (existing.getAvatar() == null) {
                    existing.setAvatar(avatarUrl);
                }
                userRepository.save(existing);
            }
            // Case: da tung login Google truoc do -> giu nguyen, tra ve luon
            return existing;
        }

        // Case: chua co tai khoan nao -> tao moi, password null
        Role role = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        User newUser = User.builder()
                .email(email)
                .fullName(fullName)
                .avatar(avatarUrl)
                .googleId(providerId)
                .password(null)
                .build();

        User savedUser;
        try {
            savedUser = userRepository.saveAndFlush(newUser);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        UserRole userRole = UserRole.builder()
                .user(savedUser)
                .role(role)
                .build();
        userRoleRepository.save(userRole);
        savedUser.setUserRoles(List.of(userRole));

        return savedUser;
    }


}
