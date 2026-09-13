package com.hnv.elearning.infrastructure.mail;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import io.netty.util.concurrent.CompleteFuture;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailService implements EmailService {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String FROM_EMAIL;

    @Override
    public void sendTextEmail(String to, String subject, String body) {

    }

    @Override
    @Async
    public CompletableFuture<Void> sendHtmlEmail(String to, String subject, String body) {

        try{
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            javaMailSender.send(message);

            log.info("Email sent successfully");
            return CompletableFuture.completedFuture(null);
        }catch (MessagingException e){
            return CompletableFuture.failedFuture(new AppException(ErrorCode.SEND_MAIL_FAIL));

        }

    }
}
