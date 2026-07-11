/**
 * Copyright (C) 2013 Loophole, LLC
 *
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * getSchSessionMap() must keep returning the live, mutable map - the same
 * "exposing internal representation" autofix that broke every terminal session for
 * UserSessionsOutput (see UserSessionsOutputTest) applies here too: SSHUtil,
 * SecureShellKtrl, SecureShellWS, and UploadAndPushKtrl all put/remove/clear active SSH
 * sessions through this reference. This test exists so that fix can't be silently reapplied.
 */
class UserSchSessionsTest {

    @Test
    void getSchSessionMapReturnsTheSameLiveMutableMapEveryTime() {
        UserSchSessions sessions = new UserSchSessions();

        Map<Integer, SchSession> map = sessions.getSchSessionMap();
        assertSame(map, sessions.getSchSessionMap());

        assertDoesNotThrow(() -> sessions.getSchSessionMap().put(1, new SchSession()));
        assertDoesNotThrow(() -> sessions.getSchSessionMap().remove(1));
    }

    @Test
    void setSchSessionMapWithNullResetsToAFreshEmptyMapRatherThanStoringNull() {
        UserSchSessions sessions = new UserSchSessions();

        sessions.setSchSessionMap(null);

        assertDoesNotThrow(() -> sessions.getSchSessionMap().put(1, new SchSession()));
    }
}
