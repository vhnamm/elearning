package com.hnv.elearning.common.otp;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import com.hnv.elearning.infrastructure.mail.EmailService;
import com.hnv.elearning.infrastructure.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Random;
import java.util.random.RandomGenerator;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;

    @Value("${otp.expiration}")
    private long otpExpirationMillis;

    @Override
    public String generateOtp() {
        return String.valueOf(new SecureRandom().nextInt(900000) + 100000);

    }

    @Override
    public void verifyOtp(String key, String otp) {
        String cachedOtp = redisService.get(key, String.class);
        if (cachedOtp == null || !passwordEncoder.matches(otp, cachedOtp)) {
            throw new AppException(ErrorCode.OTP_INVALID);
        }
        redisService.delete(key);
    }


}
