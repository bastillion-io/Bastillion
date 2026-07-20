/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.common.util;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory per-client-IP throttle for failed login attempts.
 * <p>
 * Deliberately keyed by client IP rather than username - a per-account lockout would let
 * anyone remotely lock out a known admin username by deliberately submitting the wrong
 * password for it. Tracking by IP instead slows down credential guessing without giving an
 * attacker a way to deny service to a legitimate user's account.
 */
public class LoginThrottleUtil {

    private static final int MAX_ATTEMPTS =
            Integer.parseInt(AppConfig.getProperty("maxLoginAttemptsPerIP", "10"));
    private static final long WINDOW_MILLIS =
            Long.parseLong(AppConfig.getProperty("loginThrottleWindowMinutes", "5")) * 60_000L;

    private static final ConcurrentHashMap<String, Window> ATTEMPTS = new ConcurrentHashMap<>();

    private LoginThrottleUtil() {
    }

    private static class Window {
        final long windowStart = System.currentTimeMillis();
        final AtomicInteger count = new AtomicInteger(0);

        boolean isExpired() {
            return System.currentTimeMillis() - windowStart > WINDOW_MILLIS;
        }
    }

    /**
     * true if this IP has exceeded the allowed failed login attempts within the current window
     *
     * @param clientIP client IP address
     * @return true if further login attempts from this IP should be refused for now
     */
    public static boolean isBlocked(String clientIP) {
        if (StringUtils.isEmpty(clientIP)) {
            return false;
        }
        Window window = ATTEMPTS.get(clientIP);
        if (window == null) {
            return false;
        }
        if (window.isExpired()) {
            ATTEMPTS.remove(clientIP, window);
            return false;
        }
        return window.count.get() >= MAX_ATTEMPTS;
    }

    /**
     * record a failed login attempt from this IP
     *
     * @param clientIP client IP address
     */
    public static void recordFailure(String clientIP) {
        if (StringUtils.isEmpty(clientIP)) {
            return;
        }
        ATTEMPTS.compute(clientIP, (ip, window) -> {
            if (window == null || window.isExpired()) {
                window = new Window();
            }
            window.count.incrementAndGet();
            return window;
        });
    }

    /**
     * clear any tracked failures for this IP following a successful login
     *
     * @param clientIP client IP address
     */
    public static void recordSuccess(String clientIP) {
        if (StringUtils.isNotEmpty(clientIP)) {
            ATTEMPTS.remove(clientIP);
        }
    }
}
