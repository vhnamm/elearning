package com.hnv.elearning.infrastructure.mail;

import java.util.concurrent.CompletableFuture;

public interface EmailService {
    void sendTextEmail(String to, String subject, String body);
    CompletableFuture<Void> sendHtmlEmail(String to, String subject, String body);

}
