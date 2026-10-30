package com.classroomallocation.backend.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomallocation.backend.entity.PasswordResetToken;
import com.classroomallocation.backend.entity.User;
import com.classroomallocation.backend.repository.PasswordResetTokenRepository;
import com.classroomallocation.backend.repository.UserRepository;
import com.classroomallocation.backend.security.ResetTokenCodec;

@Service
public class PasswordResetTokenService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final ResetTokenCodec tokenCodec;
    private final Duration tokenTtl;

    public PasswordResetTokenService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            ResetTokenCodec tokenCodec,
            @Value("${app.password-reset.token-ttl:15m}")
            String configuredTtl
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.tokenCodec = tokenCodec;

        this.tokenTtl = DurationStyle.detectAndParse(configuredTtl);

        if (tokenTtl.isZero()
                || tokenTtl.isNegative()
                || tokenTtl.compareTo(Duration.ofMinutes(30)) > 0) {
            throw new IllegalArgumentException(
                    "Password reset expiry must be positive "
                    + "and no longer than 30 minutes."
            );
        }
    }

    @Transactional
    public Optional<PendingResetEmail> createForEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        String normalizedEmail = email
                .trim()
                .toLowerCase(Locale.ROOT);

        Optional<User> result =
                userRepository.findByEmailForUpdate(normalizedEmail);

        if (result.isEmpty()) {
            return Optional.empty();
        }

        User user = result.get();

        if (!user.isEnabled()) {
            return Optional.empty();
        }

        String rawToken = tokenCodec.generateToken();
        String tokenHash = tokenCodec.hash(rawToken);

        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plus(tokenTtl);

        Optional<PasswordResetToken> existing =
                tokenRepository.findByUser_Id(user.getId());

        PasswordResetToken resetToken;

        if (existing.isPresent()) {
            resetToken = existing.get();

            // Replacing the hash invalidates the previous link.
            resetToken.replace(
                    tokenHash,
                    createdAt,
                    expiresAt
            );
        } else {
            resetToken = new PasswordResetToken(
                    user,
                    tokenHash,
                    createdAt,
                    expiresAt
            );
        }

        tokenRepository.saveAndFlush(resetToken);

        return Optional.of(new PendingResetEmail(
                user.getEmail(),
                rawToken,
                expiresAt
        ));
    }

    /*
     * Internal delivery data only.
     * Never return this object from a controller.
     */
    public record PendingResetEmail(
            String recipientEmail,
            String token,
            Instant expiresAt
    ) {
        @Override
        public String toString() {
            return "PendingResetEmail[details omitted]";
        }
    }
}