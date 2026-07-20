/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.common.util;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LoginThrottleUtil is the only thing standing between the login form and unlimited password
 * guessing - deliberately keyed by client IP rather than username (see
 * LoginKtrlTest/LoginKtrl.loginSubmit()), since a per-account lockout would let anyone remotely
 * lock out a known admin username by deliberately failing its password. Each test uses a fresh
 * random IP so the shared static attempt map from other tests (or reruns within the same JVM)
 * can't leak in - there's no reset hook, and none of these tests need one.
 */
class LoginThrottleUtilTest {

    // Matches the exact default-lookup LoginThrottleUtil itself uses, so this doesn't drift
    // if the bundled default in BastillionConfig.properties ever changes.
    private static final int MAX_ATTEMPTS =
            Integer.parseInt(AppConfig.getProperty("maxLoginAttemptsPerIP", "10"));

    private static String freshIp() {
        return "203.0.113." + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    void notBlockedBeforeAnyFailures() {
        assertFalse(LoginThrottleUtil.isBlocked(freshIp()));
    }

    @Test
    void notBlockedJustUnderTheThreshold() {
        String ip = freshIp();
        for (int i = 0; i < MAX_ATTEMPTS - 1; i++) {
            LoginThrottleUtil.recordFailure(ip);
        }
        assertFalse(LoginThrottleUtil.isBlocked(ip));
    }

    @Test
    void blockedOnceThresholdIsReached() {
        String ip = freshIp();
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            LoginThrottleUtil.recordFailure(ip);
        }
        assertTrue(LoginThrottleUtil.isBlocked(ip));
    }

    @Test
    void successClearsThePreviouslyRecordedFailures() {
        String ip = freshIp();
        for (int i = 0; i < MAX_ATTEMPTS - 1; i++) {
            LoginThrottleUtil.recordFailure(ip);
        }

        LoginThrottleUtil.recordSuccess(ip);

        // If the count hadn't actually reset, one more failure here would tip it over
        // MAX_ATTEMPTS - 1 + 1 = MAX_ATTEMPTS and this would already be blocked.
        LoginThrottleUtil.recordFailure(ip);
        assertFalse(LoginThrottleUtil.isBlocked(ip));
    }

    @Test
    void differentIpsAreThrottledIndependently() {
        String attacker = freshIp();
        String bystander = freshIp();
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            LoginThrottleUtil.recordFailure(attacker);
        }

        assertTrue(LoginThrottleUtil.isBlocked(attacker));
        assertFalse(LoginThrottleUtil.isBlocked(bystander));
    }

    @Test
    void nullAndEmptyIpAreNeverBlockedAndNeverThrow() {
        assertFalse(LoginThrottleUtil.isBlocked(null));
        assertFalse(LoginThrottleUtil.isBlocked(""));
        LoginThrottleUtil.recordFailure(null);
        LoginThrottleUtil.recordFailure("");
        LoginThrottleUtil.recordSuccess(null);
        LoginThrottleUtil.recordSuccess("");
    }
}
