package com.velmart.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MILLIS = 15 * 60 * 1000; // 15 minutes

    private static class Attempt {
        int count;
        long lastAttemptTime;

        Attempt(int count, long lastAttemptTime) {
            this.count = count;
            this.lastAttemptTime = lastAttemptTime;
        }
    }

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        if (key == null) return false;
        Attempt attempt = attempts.get(key.toLowerCase());
        if (attempt == null) return false;

        long now = Instant.now().toEpochMilli();
        if (now - attempt.lastAttemptTime > LOCKOUT_DURATION_MILLIS) {
            attempts.remove(key.toLowerCase());
            return false;
        }

        return attempt.count >= MAX_ATTEMPTS;
    }

    public void loginFailed(String key) {
        if (key == null) return;
        String cleanKey = key.toLowerCase();
        long now = Instant.now().toEpochMilli();
        attempts.compute(cleanKey, (k, v) -> {
            if (v == null || (now - v.lastAttemptTime > LOCKOUT_DURATION_MILLIS)) {
                return new Attempt(1, now);
            }
            v.count++;
            v.lastAttemptTime = now;
            return v;
        });
    }

    public void loginSucceeded(String key) {
        if (key != null) {
            attempts.remove(key.toLowerCase());
        }
    }
}
