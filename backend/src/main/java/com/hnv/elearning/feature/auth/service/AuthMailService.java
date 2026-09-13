package com.hnv.elearning.feature.auth.service;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import com.hnv.elearning.common.otp.OtpService;
import com.hnv.elearning.infrastructure.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthMailService {
    private final EmailService emailService;
    private final TemplateEngine templateEngine;

    public void sendAuthMail (String to, String subject, String otp) {
        String body = generateBody(otp);

        emailService.sendHtmlEmail(to, subject, body).whenComplete(
                (result, throwable) -> {
                    if (throwable != null) {
                        log.error(throwable.getMessage());
                    }
                });
    }


    private String generateBody(String otp){
        Context context = new Context();
        context.setVariable("otp", otp);
        return templateEngine.process("otp_verification", context);

    }
}
