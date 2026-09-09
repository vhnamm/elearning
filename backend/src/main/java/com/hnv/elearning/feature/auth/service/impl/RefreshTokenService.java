package com.hnv.elearning.feature.auth.service.impl;

import com.hnv.elearning.common.utils.CryptoUtil;
import com.hnv.elearning.feature.auth.entity.RefreshToken;
import com.hnv.elearning.feature.auth.repository.RefreshTokenRepository;
import com.hnv.elearning.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    @Value("${jwt.refresh-expiration}")
    private Long EXPIRATION_TIME;

    private final PasswordEncoder passwordEncoder;
    public String create(User user, String clientIp, String userAgent){
        String raw = UUID.randomUUID().toString();
        String tokenHash = CryptoUtil.sha256(raw);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .clientIp(clientIp)
                .userAgent(userAgent)
                .expiredAt(LocalDateTime.now().plus(EXPIRATION_TIME, ChronoUnit.MILLIS))
                .tokenHash(tokenHash)
                .build();

        refreshTokenRepository.save(refreshToken);

        return raw;
    }
}
