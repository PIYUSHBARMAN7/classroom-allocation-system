package com.classroomallocation.backend.controller;

import jakarta.validation.Valid;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomallocation.backend.dto.request.ResetPasswordRequest;
import com.classroomallocation.backend.security.LoginRateLimiter;
import com.classroomallocation.backend.security.ResetTokenCodec;
import com.classroomallocation.backend.service.PasswordResetService;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetService resetService;
    private final ResetTokenCodec tokenCodec;

    // Separate allowance from login and recovery requests.
    private final LoginRateLimiter resetLimiter =
            new LoginRateLimiter();

    public PasswordResetController(
            PasswordResetService resetService,
            ResetTokenCodec tokenCodec
    ) {
        this.resetService = resetService;
        this.tokenCodec = tokenCodec;
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        // Keep only the hash as the rate limiter's key.
        var decision = resetLimiter.tryAcquire(
                tokenCodec.hash(request.token())
        );

        if (!decision.allowed()) {
            return ResponseEntity.status(429)
                    .cacheControl(CacheControl.noStore())
                    .header(
                            "Retry-After",
                            Long.toString(decision.retryAfterSeconds())
                    )
                    .build();
        }

        resetService.resetPassword(
                request.token(),
                request.newPassword()
        );

        return ResponseEntity.noContent()
                .cacheControl(CacheControl.noStore())
                .build();
    }
}