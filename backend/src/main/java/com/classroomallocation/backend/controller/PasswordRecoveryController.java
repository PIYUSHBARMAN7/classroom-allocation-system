package com.classroomallocation.backend.controller;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomallocation.backend.dto.request.ForgotPasswordRequest;
import com.classroomallocation.backend.security.LoginRateLimiter;
import com.classroomallocation.backend.service.PasswordRecoveryDeliveryService;

@RestController
@RequestMapping("/api/auth")
public class PasswordRecoveryController {

    private final PasswordRecoveryDeliveryService deliveryService;

    /*
     * Independent instance: recovery requests do not consume
     * the login rate limiter's allowance.
     */
    private final LoginRateLimiter recoveryLimiter =
            new LoginRateLimiter();

    public PasswordRecoveryController(
            PasswordRecoveryDeliveryService deliveryService
    ) {
        this.deliveryService = deliveryService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        var decision = recoveryLimiter.tryAcquire(request.email());

        if (!decision.allowed()) {
            return ResponseEntity.status(429)
                    .cacheControl(CacheControl.noStore())
                    .header(
                            "Retry-After",
                            Long.toString(decision.retryAfterSeconds())
                    )
                    .body(Map.of(
                            "message",
                            "Too many recovery requests. Try again later."
                    ));
        }

        try {
            deliveryService.enqueue(request.email());
        } catch (TaskRejectedException exception) {
            return ResponseEntity.status(503)
                    .cacheControl(CacheControl.noStore())
                    .header("Retry-After", "60")
                    .body(Map.of(
                            "message",
                            "Password recovery is temporarily busy. "
                            + "Please try again later."
                    ));
        }

        return ResponseEntity.accepted()
                .cacheControl(CacheControl.noStore())
                .body(Map.of(
                        "message",
                        "If an eligible account exists for this email, "
                        + "a password reset link will be sent."
                ));
    }
}