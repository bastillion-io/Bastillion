/**
 * Copyright (C) 2014 Loophole, LLC
 *
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.util;

import org.apache.commons.codec.binary.Base32;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OTPUtil.verifyToken accepts a code from the current 30s time step or up to 3 steps (90s)
 * either side - a replay/clock-drift allowance, not an unbounded one. That boundary is the
 * actual security property worth pinning down, so these tests compute reference TOTP codes
 * for specific time steps independently (standard RFC 6238: HMAC-SHA1, dynamic truncation,
 * mod 1_000_000) rather than reaching into OTPUtil's private per-time-step method, and check
 * exactly which steps verifyToken accepts.
 */
class OTPUtilTest {

    private static long totpAt(String base32Secret, long timeStepIndex) throws Exception {
        byte[] key = new Base32().decode(base32Secret);
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key, "HmacSHA1"));
        byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(timeStepIndex).array());

        int offset = hash[hash.length - 1] & 0xF;
        long calculated = 0;
        for (int i = 0; i < 4; i++) {
            calculated <<= 8;
            calculated |= (hash[offset + i] & 0xFF);
        }
        calculated &= 0x7FFFFFFF;
        return calculated % 1000000;
    }

    private static long currentStep() {
        return System.currentTimeMillis() / 30000;
    }

    @Test
    void generateSecretProducesADecodableBase32StringAndVariesBetweenCalls() {
        String secret = OTPUtil.generateSecret();

        assertTrue(new Base32().isInAlphabet(secret));
        assertEquals(16, secret.length());
        assertNotEquals(secret, OTPUtil.generateSecret());
    }

    @Test
    void verifyTokenAcceptsTheCurrentStepsCode() throws Exception {
        String secret = OTPUtil.generateSecret();
        long token = totpAt(secret, currentStep());

        assertTrue(OTPUtil.verifyToken(secret, token));
    }

    @Test
    void verifyTokenAcceptsCodesWithinTheThreeStepWindowEitherSide() throws Exception {
        String secret = OTPUtil.generateSecret();
        long step = currentStep();

        assertTrue(OTPUtil.verifyToken(secret, totpAt(secret, step - 3)));
        assertTrue(OTPUtil.verifyToken(secret, totpAt(secret, step + 3)));
    }

    @Test
    void verifyTokenRejectsCodesOutsideTheWindow() throws Exception {
        String secret = OTPUtil.generateSecret();
        long step = currentStep();

        assertFalse(OTPUtil.verifyToken(secret, totpAt(secret, step - 4)));
        assertFalse(OTPUtil.verifyToken(secret, totpAt(secret, step + 4)));
    }

    @Test
    void verifyTokenRejectsAWrongCodeForTheRightSecret() throws Exception {
        String secret = OTPUtil.generateSecret();
        long validToken = totpAt(secret, currentStep());
        long wrongToken = (validToken + 1) % 1000000;

        assertFalse(OTPUtil.verifyToken(secret, wrongToken));
    }

    @Test
    void verifyTokenRejectsTheRightCodeForAWrongSecret() throws Exception {
        String secret = OTPUtil.generateSecret();
        String otherSecret = OTPUtil.generateSecret();
        long token = totpAt(secret, currentStep());

        assertFalse(OTPUtil.verifyToken(otherSecret, token));
    }
}
