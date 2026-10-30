package com.classroomallocation.backend.service;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetMailService {

    private static final DateTimeFormatter EXPIRY_FORMAT =
            DateTimeFormatter
                    .ofPattern("dd MMM uuuu, HH:mm 'UTC'")
                    .withZone(ZoneOffset.UTC);

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String frontendBaseUrl;

    public PasswordResetMailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String fromAddress,
            @Value("${app.frontend-base-url}") String frontendBaseUrl
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.frontendBaseUrl = validateBaseUrl(frontendBaseUrl);
    }

    public void sendResetLink(
            String recipientEmail,
            String token,
            Instant expiresAt
    ) {
        if (token == null
                || !token.matches("^[A-Za-z0-9_-]{43}$")) {
            throw new IllegalArgumentException(
                    "Invalid password reset token format."
            );
        }

        String resetLink = frontendBaseUrl
                + "/reset-password#token="
                + token;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromAddress);
        message.setTo(recipientEmail);
        message.setSubject(
                "Reset your Classroom Allocator password"
        );

        message.setText("""
                Hello,

                We received a request to reset your Classroom Allocator password.

                Open this link to choose a new password:

                %s

                This link expires at %s and can be used only once.

                If you did not request this, you can ignore this email.
                Your password has not been changed.

                Do not forward this email or share the reset link.

                Classroom Allocator
                """.formatted(
                resetLink,
                EXPIRY_FORMAT.format(expiresAt)
        ));

        mailSender.send(message);
    }

    private static String validateBaseUrl(String value) {
        URI uri = URI.create(value.trim());

        String scheme = uri.getScheme();
        String host = uri.getHost();
        String path = uri.getRawPath();

        boolean https = "https".equalsIgnoreCase(scheme);

        boolean localHttp =
                "http".equalsIgnoreCase(scheme)
                && ("localhost".equalsIgnoreCase(host)
                    || "127.0.0.1".equals(host));

        boolean validOrigin =
                host != null
                && uri.getRawUserInfo() == null
                && uri.getRawQuery() == null
                && uri.getRawFragment() == null
                && (path == null
                    || path.isEmpty()
                    || "/".equals(path));

        if ((!https && !localHttp) || !validOrigin) {
            throw new IllegalArgumentException(
                    "Frontend URL must be an HTTPS origin "
                    + "or localhost HTTP origin."
            );
        }

        String normalized = uri.toASCIIString();

        return normalized.endsWith("/")
                ? normalized.substring(0, normalized.length() - 1)
                : normalized;
    }
}