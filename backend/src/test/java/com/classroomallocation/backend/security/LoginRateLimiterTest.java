package com.classroomallocation.backend.security;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class LoginRateLimiterTest {

    @Test
    void blocksSixthAttemptForTheSameEmail() {
        AtomicLong time = new AtomicLong();
        LoginRateLimiter limiter = new LoginRateLimiter(time::get);

        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.tryAcquire("teacher@example.com").allowed());
        }

        var result = limiter.tryAcquire("teacher@example.com");

        assertFalse(result.allowed());
        assertEquals(60, result.retryAfterSeconds());
    }

    @Test
    void normalizesEmailBeforeCountingAttempts() {
        AtomicLong time = new AtomicLong();
        LoginRateLimiter limiter = new LoginRateLimiter(time::get);

        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.tryAcquire("Teacher@Example.com").allowed());
        }

        assertFalse(
                limiter.tryAcquire(" teacher@example.com ").allowed()
        );
    }

    @Test
    void blocksRequestsAcrossDifferentEmailsAtTheGlobalLimit() {
        AtomicLong time = new AtomicLong();
        LoginRateLimiter limiter = new LoginRateLimiter(time::get);

        for (int i = 0; i < 60; i++) {
            assertTrue(
                    limiter.tryAcquire("user" + i + "@example.com").allowed()
            );
        }

        assertFalse(limiter.tryAcquire("another@example.com").allowed());
    }

    @Test
    void permitsAttemptsAgainWhenTheWindowExpires() {
        AtomicLong time = new AtomicLong();
        LoginRateLimiter limiter = new LoginRateLimiter(time::get);

        for (int i = 0; i < 5; i++) {
            limiter.tryAcquire("teacher@example.com");
        }

        time.set(Duration.ofSeconds(59).toNanos());

        var blocked = limiter.tryAcquire("teacher@example.com");
        assertFalse(blocked.allowed());
        assertEquals(1, blocked.retryAfterSeconds());

        time.set(Duration.ofSeconds(60).toNanos());

        assertTrue(limiter.tryAcquire("teacher@example.com").allowed());
    }
}