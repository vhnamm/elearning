package com.hnv.elearning.feature.auth.service.impl;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import com.hnv.elearning.common.utils.CryptoUtil;
import com.hnv.elearning.feature.auth.entity.RefreshToken;
import com.hnv.elearning.feature.auth.repository.RefreshTokenRepository;
import com.hnv.elearning.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
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

    public void revoke(String refreshToken){
        String tokenHash = CryptoUtil.sha256(refreshToken);
        refreshTokenRepository.deleteByTokenHash(tokenHash);
    }

    public User validate(String rawToken){
        String tokenHash = CryptoUtil.sha256(rawToken);
        Optional<RefreshToken> optional = refreshTokenRepository.findByTokenHash(tokenHash);
        if(optional.isEmpty()){
            log.warn("Không đúng thông tin refresh token (Token không tồn tại trong DB → hoặc bị giả mạo/copy sai, hoặc đã bị revoke (logout) trước đó)");
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        RefreshToken refreshToken = optional.get();
        if(Instant.now().isAfter(Instant.from(refreshToken.getExpiredAt()))){
            log.info("Refresh token expired");

            refreshTokenRepository.deleteByTokenHash(tokenHash);
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        User user = refreshToken.getUser();

        return user;
    }
}
