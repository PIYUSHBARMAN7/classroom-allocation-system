package com.classroomallocation.backend.service;

import org.springframework.mail.SimpleMailMessage;

public interface PasswordResetEmailSender {

    void send(SimpleMailMessage message);
}