package com.classroomallocation.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final LoginRateLimiter limiter;

    public LoginRateLimitFilter(LoginRateLimiter limiter) {
        this.limiter = limiter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod())
                || !"/api/auth/login".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        LoginRateLimiter.Decision decision =
                limiter.tryAcquire(request.getParameter("email"));

        if (!decision.allowed()) {
            response.setStatus(429);
            response.setHeader(
                    "Retry-After",
                    Long.toString(decision.retryAfterSeconds())
            );
            response.setHeader("Cache-Control", "no-store");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"message\":\"Too many sign-in attempts. Try again later.\"}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}