package com.classroomallocation.backend.controller;

import com.classroomallocation.backend.dto.request.ChangePasswordRequest;
import com.classroomallocation.backend.security.LoginRateLimiter;
import com.classroomallocation.backend.service.PasswordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class PasswordController {

    private final PasswordService passwordService;
    private final SessionRegistry sessionRegistry;
    private final LoginRateLimiter rateLimiter;

    public PasswordController(
            PasswordService passwordService,
            SessionRegistry sessionRegistry,
            LoginRateLimiter rateLimiter
    ) {
        this.passwordService = passwordService;
        this.sessionRegistry = sessionRegistry;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest body,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        // Share the existing account attempt budget.
        var decision = rateLimiter.tryAcquire(authentication.getName());

        if (!decision.allowed()) {
            return ResponseEntity.status(429)
                    .header(
                            "Retry-After",
                            Long.toString(decision.retryAfterSeconds())
                    )
                    .cacheControl(CacheControl.noStore())
                    .build();
        }

        // The email comes from the session, never from the request body.
        passwordService.changePassword(authentication.getName(), body);

        sessionRegistry
                .getAllSessions(authentication.getPrincipal(), false)
                .forEach(SessionInformation::expireNow);

        new SecurityContextLogoutHandler()
                .logout(request, response, authentication);

        new CookieClearingLogoutHandler("JSESSIONID")
                .logout(request, response, authentication);

        return ResponseEntity.noContent()
                .cacheControl(CacheControl.noStore())
                .build();
    }
}