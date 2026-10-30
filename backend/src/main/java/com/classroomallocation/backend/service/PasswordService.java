package com.classroomallocation.backend.service;

import com.classroomallocation.backend.dto.request.ChangePasswordRequest;
import com.classroomallocation.backend.entity.User;
import com.classroomallocation.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import com.classroomallocation.backend.repository.PasswordResetTokenRepository;
import java.nio.charset.StandardCharsets;

@Service
public class PasswordService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final PasswordResetTokenRepository resetTokenRepository;

        public PasswordService(
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        PasswordResetTokenRepository resetTokenRepository) {
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
                this.resetTokenRepository = resetTokenRepository;
        }

        @Transactional
        @PreAuthorize("isAuthenticated()")
        public void changePassword(
                        String authenticatedEmail,
                        ChangePasswordRequest request) {
                User user = userRepository
                                .findByEmailForUpdate(authenticatedEmail)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

                if (!user.isEnabled()) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
                }

                if (!passwordEncoder.matches(
                                request.currentPassword(),
                                user.getPasswordHash())) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Password change could not be completed.");
                }

                String newPassword = request.newPassword();

                if (newPassword.codePointCount(0, newPassword.length()) < 15
                                || newPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "New password does not meet the password requirements.");
                }

                if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Choose a different password.");
                }

                resetTokenRepository.findByUser_Id(user.getId())
                                .ifPresent(resetToken -> {
                                        resetToken.markUsed(Instant.now());
                                        resetTokenRepository.save(resetToken);
                                });

                user.changePasswordHash(passwordEncoder.encode(newPassword));

                userRepository.saveAndFlush(user);
        }
}