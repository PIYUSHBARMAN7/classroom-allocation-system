package com.classroomallocation.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import com.classroomallocation.backend.repository.PasswordResetTokenRepository;
import com.classroomallocation.backend.repository.UserRepository;
import com.classroomallocation.backend.security.ResetTokenCodec;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final ResetTokenCodec tokenCodec;
    private final PasswordEncoder passwordEncoder;
    private final SessionRegistry sessionRegistry;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            ResetTokenCodec tokenCodec,
            PasswordEncoder passwordEncoder,
            SessionRegistry sessionRegistry
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.tokenCodec = tokenCodec;
        this.passwordEncoder = passwordEncoder;
        this.sessionRegistry = sessionRegistry;
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        if (rawToken == null
                || !rawToken.matches("^[A-Za-z0-9_-]{43}$")) {
            throw invalidLink();
        }

        validatePassword(newPassword);

        String suppliedHash = tokenCodec.hash(rawToken);

        // Look up only the ID before acquiring the user lock.
        Long userId = tokenRepository
                .findUserIdByTokenHash(suppliedHash)
                .orElseThrow(this::invalidLink);

        var user = userRepository
                .findByIdForUpdate(userId)
                .orElseThrow(this::invalidLink);

        if (!user.isEnabled()) {
            throw invalidLink();
        }

        /*
         * Read the token AFTER locking the user.
         * Another request may have replaced or consumed it
         * while this request waited for the lock.
         */
        var storedToken = tokenRepository
                .findByUser_Id(userId)
                .orElseThrow(this::invalidLink);

        boolean hashMatches = MessageDigest.isEqual(
                suppliedHash.getBytes(StandardCharsets.UTF_8),
                storedToken.getTokenHash()
                        .getBytes(StandardCharsets.UTF_8)
        );

        Instant now = Instant.now();

        if (!hashMatches || !storedToken.isUsableAt(now)) {
            throw invalidLink();
        }

        if (passwordEncoder.matches(
                newPassword,
                user.getPasswordHash()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Choose a different password."
            );
        }

        user.changePasswordHash(
                passwordEncoder.encode(newPassword)
        );

        storedToken.markUsed(now);

        userRepository.saveAndFlush(user);
        tokenRepository.saveAndFlush(storedToken);

        String email = user.getEmail();

        // Expire sessions only when the password update commits.
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        expireSessionsFor(email);
                    }
                }
        );
    }

    private void validatePassword(String password) {
        if (password == null
                || password.isBlank()
                || password.codePointCount(
                        0, password.length()
                ) < 15
                || password.getBytes(
                        StandardCharsets.UTF_8
                ).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must contain at least 15 characters "
                    + "and use no more than 72 UTF-8 bytes."
            );
        }
    }

    private ResponseStatusException invalidLink() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "This reset link is invalid or expired. "
                + "Request a new link."
        );
    }

    private void expireSessionsFor(String email) {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (principal instanceof UserDetails userDetails
                    && email.equals(userDetails.getUsername())) {
                sessionRegistry.getAllSessions(principal, false)
                        .forEach(SessionInformation::expireNow);
            }
        }
    }
}