package com.classroomallocation.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class PasswordRecoveryDeliveryService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    PasswordRecoveryDeliveryService.class
            );

    private final TaskExecutor executor;
    private final PasswordResetTokenService tokenService;
    private final PasswordResetMailService mailService;

    public PasswordRecoveryDeliveryService(
            @Qualifier("passwordRecoveryExecutor")
            TaskExecutor executor,
            PasswordResetTokenService tokenService,
            PasswordResetMailService mailService
    ) {
        this.executor = executor;
        this.tokenService = tokenService;
        this.mailService = mailService;
    }

    public void enqueue(String email) {
        // Queue every accepted request before checking account existence.
        executor.execute(() -> deliver(email));
    }

    private void deliver(String email) {
        try {
            /*
             * The transactional service commits the token before
             * returning. Email delivery happens after that commit.
             */
            var pending = tokenService.createForEmail(email);

            if (pending.isEmpty()) {
                return;
            }

            var delivery = pending.get();

            mailService.sendResetLink(
                    delivery.recipientEmail(),
                    delivery.token(),
                    delivery.expiresAt()
            );
        } catch (RuntimeException exception) {
            // Do not log email addresses, tokens or SMTP credentials.
            log.error(
                    "Password recovery processing failed. "
                    + "Check database connectivity and mail configuration."
            );
        }
    }
}