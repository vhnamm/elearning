package com.hnv.elearning.feature.auth.service;

import com.hnv.elearning.common.otp.OtpService;
import com.hnv.elearning.infrastructure.mail.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

@Service
@RequiredArgsConstructor
public class AuthMailService {
    private final EmailService emailService;
    private final OtpService otpService;
    private final TemplateEngine templateEngine;

    public void sendAuthMail (String from, String to, String subject) {
        String otp = otpService.generateOtp();

    }

    public String generateBody(String otp)
}
