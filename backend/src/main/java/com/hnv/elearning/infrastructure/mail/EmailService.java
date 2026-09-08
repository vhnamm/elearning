package com.hnv.elearning.infrastructure.mail;

public interface EmailService {
    void sendTextEmail(String to, String subject, String body);
    void sendHtmlEmail(String to, String subject, String body);

}
