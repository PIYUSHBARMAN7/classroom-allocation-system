package com.classroomallocation.backend.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.LongSupplier;

@Component
public class LoginRateLimiter {

    private static final int PER_EMAIL_LIMIT = 5;
    private static final int GLOBAL_LIMIT = 60;
    private static final long WINDOW_NANOS =
            Duration.ofSeconds(60).toNanos();

    private final LongSupplier ticker;
    private final Map<String, Integer> attemptsByEmail = new HashMap<>();

    private long windowStarted;
    private int totalAttempts;

    public LoginRateLimiter() {
        this(System::nanoTime);
    }

    LoginRateLimiter(LongSupplier ticker) {
        this.ticker = ticker;
        this.windowStarted = ticker.getAsLong();
    }

    public synchronized Decision tryAcquire(String email) {
        long now = ticker.getAsLong();
        long elapsed = now - windowStarted;

        if (elapsed >= WINDOW_NANOS) {
            attemptsByEmail.clear();
            totalAttempts = 0;
            windowStarted = now;
            elapsed = 0;
        }

        long retryAfterSeconds = Math.max(
                1,
                (WINDOW_NANOS - elapsed + 999_999_999L)
                        / 1_000_000_000L
        );

        if (totalAttempts >= GLOBAL_LIMIT) {
            return new Decision(false, retryAfterSeconds);
        }

        totalAttempts++;

        String key = email == null
                ? ""
                : email.trim().toLowerCase(Locale.ROOT);

        int attempts = attemptsByEmail.getOrDefault(key, 0);

        if (attempts >= PER_EMAIL_LIMIT) {
            return new Decision(false, retryAfterSeconds);
        }

        attemptsByEmail.put(key, attempts + 1);

        return new Decision(true, 0);
    }

    public record Decision(boolean allowed, long retryAfterSeconds) {
    }
}