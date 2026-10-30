package com.classroomallocation.backend.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        name = "app.mail.provider",
        havingValue = "smtp",
        matchIfMissing = true
)
public class SmtpPasswordResetEmailSender
        implements PasswordResetEmailSender {

    private final JavaMailSender mailSender;

    public SmtpPasswordResetEmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(SimpleMailMessage message) {
        mailSender.send(message);
    }
}