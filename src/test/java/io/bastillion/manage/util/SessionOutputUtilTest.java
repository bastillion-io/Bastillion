/**
 * Copyright (C) 2015 Loophole, LLC
 *
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.util;

import io.bastillion.manage.model.SessionOutput;
import io.bastillion.manage.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SessionOutputUtil buffers SSH terminal output in memory until the browser's ajax poll
 * picks it up (and, per the terminal-replay stored XSS fix, that buffered text is later
 * rendered with HTML-escaping applied at render time - not here). These tests pin down the
 * storage-layer contract: raw output passes through unmodified, and getOutput() drains the
 * buffer it returns rather than leaving stale text to double up on the next poll. Each test
 * uses its own session id since userSessionsOutputMap is static/shared for the whole test JVM.
 */
class SessionOutputUtilTest {

    private static User testUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        return user;
    }

    private static SessionOutput newInstance(long sessionId, int instanceId) {
        SessionOutput sessionOutput = new SessionOutput();
        sessionOutput.setSessionId(sessionId);
        sessionOutput.setInstanceId(instanceId);
        return sessionOutput;
    }

    @Test
    void getOutputReturnsBufferedTextUnmodified() throws Exception {
        long sessionId = 9001L;
        SessionOutputUtil.addOutput(newInstance(sessionId, 1));

        String raw = "<script>alert(document.cookie)</script>";
        char[] chars = raw.toCharArray();
        SessionOutputUtil.addToOutput(sessionId, 1, chars, 0, chars.length);

        List<SessionOutput> output = SessionOutputUtil.getOutput(null, sessionId, testUser());

        assertEquals(1, output.size());
        assertEquals(raw, output.get(0).getOutput().toString());
    }

    @Test
    void getOutputDrainsTheBufferSoASecondCallReturnsNothingNew() throws Exception {
        long sessionId = 9002L;
        SessionOutputUtil.addOutput(newInstance(sessionId, 1));
        char[] chars = "first batch".toCharArray();
        SessionOutputUtil.addToOutput(sessionId, 1, chars, 0, chars.length);

        List<SessionOutput> first = SessionOutputUtil.getOutput(null, sessionId, testUser());
        assertEquals(1, first.size());

        List<SessionOutput> second = SessionOutputUtil.getOutput(null, sessionId, testUser());
        assertTrue(second.isEmpty());
    }

    @Test
    void emptyOutputBufferIsExcludedFromResults() throws Exception {
        long sessionId = 9003L;
        SessionOutputUtil.addOutput(newInstance(sessionId, 1));

        List<SessionOutput> output = SessionOutputUtil.getOutput(null, sessionId, testUser());

        assertTrue(output.isEmpty());
    }

    @Test
    void addToOutputOnlyAppendsTheGivenSlice() throws Exception {
        long sessionId = 9004L;
        SessionOutputUtil.addOutput(newInstance(sessionId, 1));

        // Simulates a partially-filled read buffer, e.g. a char[1024] read buffer where only
        // the first 5 bytes of this particular read were valid.
        char[] readBuffer = "hello-garbage-past-here".toCharArray();
        SessionOutputUtil.addToOutput(sessionId, 1, readBuffer, 0, 5);

        List<SessionOutput> output = SessionOutputUtil.getOutput(null, sessionId, testUser());

        assertEquals("hello", output.get(0).getOutput().toString());
    }

    @Test
    void removeOutputOnlyRemovesTheGivenInstance() throws Exception {
        long sessionId = 9005L;
        SessionOutputUtil.addOutput(newInstance(sessionId, 1));
        SessionOutputUtil.addOutput(newInstance(sessionId, 2));
        SessionOutputUtil.addToOutput(sessionId, 1, "one".toCharArray(), 0, 3);
        SessionOutputUtil.addToOutput(sessionId, 2, "two".toCharArray(), 0, 3);

        SessionOutputUtil.removeOutput(sessionId, 1);

        List<SessionOutput> output = SessionOutputUtil.getOutput(null, sessionId, testUser());
        assertEquals(1, output.size());
        assertEquals(2, output.get(0).getInstanceId());
        assertEquals("two", output.get(0).getOutput().toString());
    }

    @Test
    void removeUserSessionClearsTheWholeSession() throws Exception {
        long sessionId = 9006L;
        SessionOutputUtil.addOutput(newInstance(sessionId, 1));
        SessionOutputUtil.addToOutput(sessionId, 1, "text".toCharArray(), 0, 4);

        SessionOutputUtil.removeUserSession(sessionId);

        // No session tracked anymore - getOutput must not throw, just return nothing.
        List<SessionOutput> output = SessionOutputUtil.getOutput(null, sessionId, testUser());
        assertTrue(output.isEmpty());

        // addToOutput against a removed session is a safe no-op, not an NPE.
        SessionOutputUtil.addToOutput(sessionId, 1, "text".toCharArray(), 0, 4);
    }
}
